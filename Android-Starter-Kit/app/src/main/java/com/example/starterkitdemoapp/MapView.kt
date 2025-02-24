package com.example.starterkitdemoapp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.geojson.Feature
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.extension.compose.MapState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.plugin.gestures.gestures
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapView() {
    var showResetButton by remember { mutableStateOf(false) }
    var clickedFeature by remember { mutableStateOf<Feature?>(null) }

    // Initial map state & reset value
    val initialCameraOptions = CameraOptions.Builder()
        .center(Point.fromLngLat(-71.09290, 42.34622))
        .pitch(0.0)
        .zoom(8.5)
        .bearing(0.0)
        .build()

    // Create and remember the MapViewportState
    val mapViewportState = rememberMapViewportState {
        setCameraOptions(initialCameraOptions)
    }

    // Create and remember the MapState
    val mapState = rememberMapState()
    val coroutineScope = rememberCoroutineScope()

    val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.PartiallyExpanded,
                // Set skipHiddenState to false to allow animating to hidden
                skipHiddenState = false
            ),
            snackbarHostState = remember { SnackbarHostState() }
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            val feature = clickedFeature // Capture the current value of clickedFeature
            if (feature != null) {
                DrawerView(feature = clickedFeature) {
                    // Close bottomSheet and clear feature data
                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.hide()
                        clickedFeature = null
                    }

                }
            }
        },
        sheetPeekHeight = 0.dp // Start with the bottom sheet hidden
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                // This Style URL loads a custom map style created in Mapbox Studio https://console.mapbox.com/studio
                // The style contains our custom tileset (GeoJson data) and Map styles (markers, fonts, etc..)
                style = {
                    MapStyle(style = "mapbox://styles/examples/cm37hh1nx017n01qk2hngebzt")
                },
                scaleBar = { }, // turn off the scale bar
                compass = { },  // turn off the compass
                mapViewportState = mapViewportState,
                mapState = mapState, // Use MapState for interactions
                onMapClickListener = {
                    coroutineScope.launch {
                        val feature: Feature? = mapState.queryFeaturesAt(it)
                        if (feature == null) {
                            Log.d("debug", "No feature found at clicked location")
                        } else {
                            // Extract properties
                            clickedFeature = feature
                            val properties = feature.properties()?.toString()
                            Log.d("debug", "Feature found with properties: $properties")

                            // Expand bottom sheet or update UI
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                    false
                }
            ) {
                DisposableMapEffect(key1 = clickedFeature) { mapView ->
                    val gesturesPlugin = mapView.gestures

                    // Add the move listener to the gestures plugin
                    val moveListener = object : OnMoveListener {
                        override fun onMoveBegin(detector: MoveGestureDetector) {
                            showResetButton = true
                        }

                        override fun onMove(detector: MoveGestureDetector): Boolean {
                            // default return
                            return false
                        }

                        override fun onMoveEnd(detector: MoveGestureDetector) {
                            // default return
                        }
                    }
                    gesturesPlugin.addOnMoveListener(moveListener)

                    // Cleanup listeners when the composable is disposed
                    onDispose {
                        gesturesPlugin.removeOnMapClickListener { true }
                    }
                }
            }

            // If Map has been interacted with
            if (showResetButton) {
                Button(
                    onClick = {
                        // Reset the camera position to the initial state
                        mapViewportState.flyTo(
                            cameraOptions = initialCameraOptions,
                            MapAnimationOptions.mapAnimationOptions { duration(4000) }
                        )
                        showResetButton = false
                        clickedFeature = null
                        Log.d("MapView", "Reset button clicked")
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text("Reset Map")
                }
            }
        }
    }
}


private suspend fun MapState.queryFeaturesAt(point: Point): Feature? =
    queryRenderedFeatures(
        RenderedQueryGeometry(pixelForCoordinate(point)),
        RenderedQueryOptions(listOf("dog-groomers-boston-marker"), null)
    ).value?.let { list ->
        list.firstNotNullOfOrNull {
            // Log.d(null, "Feature properties: ${it.queriedFeature.feature.properties()}")
            it.queriedFeature.feature
        }
    }

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MapView()
}