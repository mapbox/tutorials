//
//  TileRegionDownloadView.swift
//  simple-map-swiftui
//
//  Created on 8/12/25.
//

import SwiftUI
import MapboxMaps

// offline region configuration struct
struct OfflineRegion {
    let id: String
    let name: String
    let bounds: CoordinateBounds
    
    // convert CoordinateBounds to Polygon
    var polygon: Polygon {
        let coordinates = [
            CLLocationCoordinate2D(latitude: bounds.southwest.latitude, longitude: bounds.southwest.longitude),
            CLLocationCoordinate2D(latitude: bounds.southwest.latitude, longitude: bounds.northeast.longitude),
            CLLocationCoordinate2D(latitude: bounds.northeast.latitude, longitude: bounds.northeast.longitude),
            CLLocationCoordinate2D(latitude: bounds.northeast.latitude, longitude: bounds.southwest.longitude),
            CLLocationCoordinate2D(latitude: bounds.southwest.latitude, longitude: bounds.southwest.longitude)
        ]
        return Polygon([coordinates])
    }
}

// MARK: - View Model

class TileRegionDownloadViewModel: ObservableObject {
    @Published var downloadingRegions: Set<String> = []
    @Published var downloadProgress: [String: Float] = [:]
    @Published var refreshTrigger = false

    // define regions
    let regions = [
        OfflineRegion(
            id: "new-york-region",
            name: "New York",
            bounds: CoordinateBounds(
                southwest: CLLocationCoordinate2D(
                    latitude:  40.48398,
                    longitude: -74.28127
                ),
                northeast: CLLocationCoordinate2D(
                    latitude: 40.98701,
                    longitude: -73.58442
                )
            )
        ),
        OfflineRegion(
            id: "london-region",
            name: "London",
            bounds: CoordinateBounds(
                southwest: CLLocationCoordinate2D(latitude: 51.4874, longitude: -0.1278),
                northeast: CLLocationCoordinate2D(latitude: 51.5174, longitude: -0.0978)
            )
        ),
        OfflineRegion(
            id: "paris-region",
            name: "Paris",
            bounds: CoordinateBounds(
                southwest: CLLocationCoordinate2D(latitude: 48.8366, longitude: 2.3522),
                northeast: CLLocationCoordinate2D(latitude: 48.8666, longitude: 2.3822)
            )
        )
    ]

    func downloadRegion(region: OfflineRegion) {
        OfflineRegionManager.downloadRegion(
            region: region,
            downloadingRegions: downloadingRegions,
            onDownloadingRegionsUpdate: { [weak self] updatedSet in
                DispatchQueue.main.async {
                    self?.downloadingRegions = updatedSet
                }
            },
            onProgress: { [weak self] regionId, progress in
                DispatchQueue.main.async {
                    self?.downloadProgress[regionId] = progress
                }
            },
            onCompletion: { [weak self] regionId, result in
                DispatchQueue.main.async {
                    self?.downloadProgress.removeValue(forKey: regionId)
                    
                    switch result {
                    case .success(let tileRegion):
                        print("Downloaded \(region.name): \(tileRegion.completedResourceSize) bytes")
                    case .failure(let error):
                        print("Failed to download \(region.name): \(error)")
                    }
                }
            }
        )
    }

    func clearAllRegions() {
        OfflineRegionManager.clearAllRegions { [weak self] in
            DispatchQueue.main.async {
                self?.downloadingRegions.removeAll()
                self?.downloadProgress.removeAll()
                self?.refreshTrigger.toggle()
            }
        }
    }
}

// MARK: - Main View

struct TileRegionDownloadView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = TileRegionDownloadViewModel()

    var body: some View {
        NavigationView {
            List {
                ForEach(viewModel.regions, id: \.id) { region in
                    RegionRowView(
                        region: region,
                        isDownloading: viewModel.downloadingRegions.contains(region.id),
                        progress: viewModel.downloadProgress[region.id] ?? 0.0,
                        refreshTrigger: viewModel.refreshTrigger,
                        onDownload: {
                            viewModel.downloadRegion(region: region)
                        }
                    )
                }
            }
            .navigationTitle("Offline Regions")
            .toolbarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Done", role: .cancel) {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Clear All", role: .destructive) {
                        viewModel.clearAllRegions()
                    }
                    .tint(.red)
                }
            }
        }
    }
}

// MARK: - Row View

struct RegionRowView: View {
    let region: OfflineRegion
    let isDownloading: Bool
    let progress: Float
    let refreshTrigger: Bool
    let onDownload: () -> Void

    @State private var isDownloaded = false
    @State private var sizeInMB: Double = 0.0

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(region.name)
                    .font(.headline)
                
                if isDownloaded {
                    Text("Downloaded • \(String(format: "%.1f", sizeInMB)) MB")
                        .font(.caption)
                        .foregroundColor(.green)
                } else if isDownloading {
                    Text("Downloading...")
                        .font(.caption)
                        .foregroundColor(.blue)
                } else {
                    Text("Not downloaded")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
            }
            
            Spacer()
            
            if isDownloading {
                VStack {
                    ProgressView()
                        .scaleEffect(0.8)
                    Text("\(Int(progress * 100))%")
                        .font(.caption2)
                        .foregroundColor(.blue)
                }
            } else if isDownloaded {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundColor(.green)
                    .font(.title2)
            } else {
                Button(action: onDownload) {
                    Text("Download")
                        .font(.caption)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(6)
                }
            }
        }
        .padding(.vertical, 4)
        .onAppear {
            checkIfDownloaded()
        }
        .onChange(of: isDownloading) { downloading in
            if !downloading {
                checkIfDownloaded()
            }
        }
        .onChange(of: refreshTrigger) { _ in
            checkIfDownloaded()
        }
    }

    private func checkIfDownloaded() {
        let tileStore = TileStore.default
        tileStore.tileRegion(forId: region.id) { result in
            DispatchQueue.main.async {
                switch result {
                case .success(let tileRegion):
                    self.isDownloaded = true
                    self.sizeInMB = Double(tileRegion.completedResourceSize) / (1024 * 1024)
                case .failure(_):
                    self.isDownloaded = false
                    self.sizeInMB = 0.0
                }
            }
        }
    }
}

