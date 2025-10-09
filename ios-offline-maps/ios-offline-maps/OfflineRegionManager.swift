//
//  OfflineRegionManager.swift
//  simple-map-swiftui
//
//  Created on 8/12/25.
//

import Foundation
import MapboxMaps

class OfflineRegionManager {
    
    private static var hasDownloadedStylePack = false
    
    static func ensureStylePackDownloaded() {
        // Only download once per app session
        if hasDownloadedStylePack {
            return
        }
        
        let offlineManager = OfflineManager()
        
        let stylePackLoadOptions = StylePackLoadOptions(
            glyphsRasterizationMode: .ideographsRasterizedLocally,
            metadata: ["name": "mapbox-standard-stylepack"],
            acceptExpired: false
        )
        
        offlineManager.loadStylePack(
            for: .standard,
            loadOptions: stylePackLoadOptions!
        ) { _ in } completion: { result in
            switch result {
            case let .success(stylePack):
                // Style pack download finishes successfully
                print("Downloaded style pack: \(stylePack)")
                hasDownloadedStylePack = true
                
            case let .failure(error):
                // Handle error occurred during the style pack download
                if case StylePackError.canceled = error {
                    print("Style pack download cancelled")
                } else {
                    print("Style pack download failed: \(error)")
                }
            }
        }
    }
    
    static func downloadRegion(
        region: OfflineRegion,
        downloadingRegions: Set<String>,
        onDownloadingRegionsUpdate: @escaping (Set<String>) -> Void,
        onProgress: @escaping (String, Float) -> Void,
        onCompletion: @escaping (String, Result<TileRegion, Error>) -> Void
    ) {
        // Don't start if already downloading
        if downloadingRegions.contains(region.id) {
            return
        }
        
        let tileStore = TileStore.default
        let offlineManager = OfflineManager()
        
        // Create tileset descriptor
        let tilesetDescriptor = offlineManager.createTilesetDescriptor(
            for: TilesetDescriptorOptions(
                styleURI: .standard, // get tiles for the Mapbox Standard style
                zoomRange: 10...14,
                tilesets: []
            )
        )
        
        // Create load options using the region's polygon and metadata
        let loadOptions = TileRegionLoadOptions(
            geometry: .polygon(region.polygon),
            descriptors: [tilesetDescriptor],
            metadata: ["name": region.name],
            acceptExpired: true
        )!
        
        // Start downloading - notify UI to add to downloading set
        var updatedDownloadingRegions = downloadingRegions
        updatedDownloadingRegions.insert(region.id)
        onDownloadingRegionsUpdate(updatedDownloadingRegions)
        onProgress(region.id, 0.0)
        
        let _ = tileStore.loadTileRegion(
            forId: region.id,
            loadOptions: loadOptions
        ) { progress in
            let totalResources = max(progress.requiredResourceCount, 1)
            let progressValue = Float(progress.completedResourceCount) / Float(totalResources)
            onProgress(region.id, progressValue)
        } completion: { result in
            // Remove from downloading set and notify completion
            updatedDownloadingRegions.remove(region.id)
            onDownloadingRegionsUpdate(updatedDownloadingRegions)
            onCompletion(region.id, result)
        }
    }
    
    static func clearAllRegions(onCompletion: @escaping () -> Void) {
        let tileStore = TileStore.default
        
        // Get all tile regions from the store
        tileStore.allTileRegions { result in
            switch result {
            case .success(let tileRegions):
                // Remove each region found in the store
                for tileRegion in tileRegions {
                    tileStore.removeRegion(forId: tileRegion.id) { removeResult in
                        switch removeResult {
                        case .success:
                            print("Removed region: \(tileRegion.id)")
                        case .failure(let error):
                            print("Failed to remove region \(tileRegion.id): \(error)")
                        }
                    }
                }
                
                // Clear ambient cache after removing regions
                tileStore.clearAmbientCache { cacheResult in
                    switch cacheResult {
                    case .success(let bytes):
                        print("Cleared \(bytes) bytes from cache")
                    case .failure(let error):
                        print("Failed to clear cache: \(error)")
                    }
                    onCompletion()
                }
                
            case .failure(let error):
                print("Failed to get tile regions: \(error)")
                onCompletion()
            }
        }
    }
}
