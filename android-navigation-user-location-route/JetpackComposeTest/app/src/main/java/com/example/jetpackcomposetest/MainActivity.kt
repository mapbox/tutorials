package com.example.jetpackcomposetest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import kotlinx.coroutines.launch
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

// Define Destination data class
data class Destination(val name: String, val location: Point)

// Hard-coded list of destinations
val destinations = listOf(
    Destination("Boston Common", Point.fromLngLat(-71.06601, 42.35489)),
    Destination("Harvard Square", Point.fromLngLat(-71.12016, 42.37259)),
    Destination("Bunker Hill Monument", Point.fromLngLat(-71.06083, 42.37632))
)

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .build()
        ).attach(this)

        setContent {
            val origin = remember { mutableStateOf(Point.fromLngLat(-71.07698, 42.35078)) }
            val destination = remember { mutableStateOf<Point?>(null) }
            val routes = remember { mutableStateOf<List<NavigationRoute>?>(null) } // <- Added state for routes
            val coroutineScope = rememberCoroutineScope()

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Choose a destination",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    destinations.forEach { dest ->
                        Button(
                            onClick = {
                                destination.value = dest.location
                                coroutineScope.launch {
                                    if (MapboxNavigationApp.isSetup()) {
                                        val mapboxNavigation = MapboxNavigationApp.current()
                                        Log.d("FOO", mapboxNavigation.toString())

                                        if (mapboxNavigation != null) {
                                            try {
                                                val resultRoutes = NavigationLoader.loadRoutes(
                                                    mapboxNavigation,
                                                    origin = origin.value,
                                                    destination = destination.value!!
                                                )
                                                println("Routes successfully loaded: ${resultRoutes.size}")

                                                // ✅ Update state with routes
                                                routes.value = resultRoutes
                                            } catch (e: Exception) {
                                                println("Failed to load routes: ${e.message}")
                                            }
                                        } else {
                                            println("MapboxNavigationApp.current() returned null unexpectedly.")
                                        }
                                    } else {
                                        println("MapboxNavigationApp is not setup yet.")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = dest.name)
                        }
                    }
                }

                // ✅ Full screen panel when routes exist
                if (routes.value != null) {
                    val mapboxNavigation = MapboxNavigationApp.current()

                    if (mapboxNavigation != null) {
                        OpenMap(
                            routes = routes.value,
                            mapboxNavigation = mapboxNavigation
                        )
                    }
                }
            }
        }
    }
}
