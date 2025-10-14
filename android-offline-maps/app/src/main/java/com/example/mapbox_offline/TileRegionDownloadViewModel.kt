package com.example.mapbox_offline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CoordinateBounds
import com.mapbox.common.TileRegion
import android.util.Log

class TileRegionDownloadViewModel : ViewModel() {
    private val TAG = "TileRegionVM"

    var downloadingRegions by mutableStateOf(setOf<String>())
        private set

    var downloadProgress by mutableStateOf(mapOf<String, Float>())
        private set

    var refreshTrigger by mutableStateOf(false)
        private set

    val regions = listOf(
        OfflineRegion(
            id = "new-york-region",
            name = "New York",
            bounds = CoordinateBounds(
                Point.fromLngLat(-74.28127, 40.48398),
                Point.fromLngLat(-73.58442, 40.98701)
            )
        ),
        OfflineRegion(
            id = "london-region",
            name = "London",
            bounds = CoordinateBounds(
                Point.fromLngLat(-0.1278, 51.4874),
                Point.fromLngLat(-0.0978, 51.5174)
            )
        ),
        OfflineRegion(
            id = "paris-region",
            name = "Paris",
            bounds = CoordinateBounds(
                Point.fromLngLat(2.3522, 48.8366),
                Point.fromLngLat(2.3822, 48.8666)
            )
        )
    )

    fun downloadRegion(region: OfflineRegion) {
        OfflineRegionManager.downloadRegion(
            region = region,
            downloadingRegions = downloadingRegions,
            onDownloadingRegionsUpdate = { updatedSet ->
                // replace whole set so Compose re-composes
                downloadingRegions = updatedSet
            },
            onProgress = { regionId, progress ->
                downloadProgress = downloadProgress + (regionId to progress)
            },
            onCompletion = { regionId, result ->
                // remove progress entry on completion
                downloadProgress = downloadProgress - regionId
                when {
                    result.isSuccess -> {
                        val tileRegion: TileRegion? = result.getOrNull()
                        Log.i(TAG, "Downloaded ${region.name}: ${tileRegion?.completedResourceSize}")
                    }
                    result.isFailure -> {
                        Log.e(TAG, "Failed to download ${region.name}: ${result.exceptionOrNull()}")
                    }
                }
            }
        )
    }

    fun clearAllRegions() {
        OfflineRegionManager.clearAllRegions {
            downloadingRegions = emptySet()
            downloadProgress = emptyMap()
            refreshTrigger = !refreshTrigger
        }
    }
}
