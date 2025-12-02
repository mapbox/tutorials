package com.example.mapbox_offline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

public class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure Mapbox standard style pack is present (no-op if already downloaded).
        OfflineRegionManager.ensureStylePackDownloaded(this)
        setContent {
            var showingDownload by remember { mutableStateOf(false) }

            // Ensure system back button will dismiss the modal when open
            if (showingDownload) {
                BackHandler { showingDownload = false }
            }

            Box(Modifier.fillMaxSize()) {
                MapboxMap(
                    Modifier.fillMaxSize(),
                    mapViewportState = rememberMapViewportState {
                        setCameraOptions {
                            zoom(2.0)
                            center(Point.fromLngLat(-98.0, 39.5))
                            pitch(0.0)
                            bearing(0.0)
                        }
                    },
                )

                // Download button overlay (top-right)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // moved down by increasing top padding
                        .padding(start = 12.dp, end = 12.dp, top = 72.dp, bottom = 12.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Button(onClick = { showingDownload = true }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Manage Offline Regions")
                    }
                }

                // Present the TileRegionDownloadScreen as a full-screen overlay that completely covers the map
                if (showingDownload) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // pass onDone so the top-left arrow dismisses the modal
                            TileRegionDownloadScreen(onDone = { showingDownload = false })
                        }
                    }
                }

            }
        }
    }
}