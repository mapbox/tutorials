package com.example.mapbox_offline

import android.content.Context
import android.util.Log
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Geometry
import com.mapbox.maps.Style
import com.mapbox.common.TileRegion
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.common.TilesetDescriptor
import com.mapbox.maps.TilesetDescriptorOptions
import com.mapbox.maps.StylePackLoadOptions
import java.util.concurrent.atomic.AtomicReference
import com.mapbox.maps.GlyphsRasterizationMode
import com.mapbox.maps.OfflineManager
import com.mapbox.common.MapboxOptions
import com.mapbox.maps.mapsOptions


object OfflineRegionManager {
    private const val TAG = "OfflineRegionManager"
    private val offlineManager: OfflineManager = OfflineManager()
    private val tileStore: TileStore? = MapboxOptions.mapsOptions.tileStore!!


    fun ensureStylePackDownloaded(context: Context) {
        try {
            val stylePackOptions = StylePackLoadOptions.Builder()
                .glyphsRasterizationMode(GlyphsRasterizationMode.IDEOGRAPHS_RASTERIZED_LOCALLY)
                .metadata(Value("mapbox-standard-stylepack"))
                .acceptExpired(false)
                .build()

            offlineManager.loadStylePack(
                Style.STANDARD,
                stylePackOptions,
                { progress ->
                    //
                },
                { expected ->
                    expected.value?.let { stylePack ->
                        // Style pack download finishes successfully
                        Log.d(TAG, "Style pack downloaded: $stylePack")
                    }
                    expected.error?.let {
                        // Handle error occurred during the style pack download.
                    }
                }
            )

        } catch (e: Throwable) {
            Log.w(TAG, "Style pack API not available or failed: ${e.message}")
        }
    }

    fun downloadRegion(
        region: OfflineRegion,
        downloadingRegions: Set<String>,
        onDownloadingRegionsUpdate: (Set<String>) -> Unit,
        onProgress: (String, Float) -> Unit,
        onCompletion: (String, Result<TileRegion>) -> Unit
    ) {
        if (downloadingRegions.contains(region.id)) {
            Log.d(TAG, "Region ${region.id} is already downloading; skipping")
//            return
        }

        val tilesetDescriptorOptions = TilesetDescriptorOptions.Builder()
            .styleURI(Style.STANDARD)
            .minZoom(10)
            .maxZoom(14)
            .build()

        val tilesetDescriptor = offlineManager.createTilesetDescriptor(tilesetDescriptorOptions)

        

        val loadOptions: TileRegionLoadOptions =TileRegionLoadOptions.Builder()
                .geometry(region.polygon as Geometry)
                .descriptors(listOf(tilesetDescriptor))
                .metadata(Value(region.name))
                .acceptExpired(true)
                .build()
  

        val updated = downloadingRegions.toMutableSet()
        updated.add(region.id)
        onDownloadingRegionsUpdate(updated)
        onProgress(region.id, 0.0f)

        // Ensure tileStore is available before calling its methods
        val ts = tileStore
        if (ts == null) {
            Log.e(TAG, "TileStore not available")
            val updatedAfter = updated.toMutableSet()
            updatedAfter.remove(region.id)
            onDownloadingRegionsUpdate(updatedAfter)
            onCompletion(region.id, Result.failure(Exception("TileStore not available")))
            return
        }
        // Safe to use non-null ts from here on
        ts.loadTileRegion(
             region.id,
             loadOptions,
             { progress ->
                 val total = maxOf(progress.requiredResourceCount, 1)
                 val progressVal = progress.completedResourceCount.toFloat() / total.toFloat()
                 onProgress(region.id, progressVal)
             }
         ) { result ->
             val updatedAfter = updated.toMutableSet()
             updatedAfter.remove(region.id)
             onDownloadingRegionsUpdate(updatedAfter)

             if (result.isValue) {
                 val tileRegion = result.value!!
                 onCompletion(region.id, Result.success(tileRegion))
             } else {
                // result.error may be a Serializable (not a Throwable). Convert safely to Throwable.
                val rawError = result.error
                val throwable = when (rawError) {
                    is Throwable -> rawError
                    null -> Exception("Unknown error")
                    else -> Exception(rawError.toString())
                }
                onCompletion(region.id, Result.failure(throwable))
             }
         }

    }

    fun clearAllRegions(onCompletion: () -> Unit) {
        tileStore?.getAllTileRegions { result ->
            if (result.isValue) {
                val tileRegions = result.value ?: emptyList()
                if (tileRegions.isEmpty()) {
                    tileStore.clearAmbientCache { cacheResult ->
                        if (cacheResult.isValue) {
                            Log.i(TAG, "Cleared ${cacheResult.value} bytes from cache")
                        } else {
                            Log.e(TAG, "Failed to clear cache: ${cacheResult.error}")
                        }
                        onCompletion()
                    }
//                    return
                }

                val removalsPending = AtomicReference(tileRegions.size)
                for (tileRegion in tileRegions) {
                    tileStore.removeTileRegion(tileRegion.id) { removeResult ->
                        if (removeResult.isValue) {
                            Log.i(TAG, "Removed region: ${tileRegion.id}")
                        } else {
                            Log.e(TAG, "Failed to remove region ${tileRegion.id}: ${removeResult.error}")
                        }
                        val remaining = removalsPending.updateAndGet { it - 1 }
                        if (remaining <= 0) {
                            tileStore.clearAmbientCache { cacheResult ->
                                if (cacheResult.isValue) {
                                    Log.i(TAG, "Cleared ${cacheResult.value} bytes from cache")
                                } else {
                                    Log.e(TAG, "Failed to clear cache: ${cacheResult.error}")
                                }
                                onCompletion()
                            }
                        }
                    }
                }
            } else {
                Log.e(TAG, "Failed to get tile regions: ${result.error}")
                onCompletion()
            }
        }
    }
}
