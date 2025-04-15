package com.example.geojsonapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.geojson.FeatureCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class MainActivity : ComponentActivity() {
    // ModalBottomSheet is an experimental class from Material3, this code may be changed in future updates.
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            var showBottomSheet by remember { mutableStateOf(false) }

            // Global variables to update ModalBottomSheet with data from GeoJSON when clicking on a marker.
            val locationName = remember {
                mutableStateOf("none")
            }
            val locationAddress = remember {
                mutableStateOf("none")
            }
            val locationPhoneNumber = remember {
                mutableStateOf("none")
            }

            var featureCollection: FeatureCollection? by remember {
                mutableStateOf(null)
            }

            // Grabs the image used for the marker
            val markerImage = rememberIconImage(
                resourceId = R.drawable.ic_blue_marker
            )

            LaunchedEffect(Unit) {
                // Grabs the GeoJSON file so its data can be accessed to create the markers
                val geoJson = withContext(Dispatchers.IO) {
                    assets.open("coffee_shops.geojson").bufferedReader().use { it.readText() }
                }
                featureCollection = FeatureCollection.fromJson(geoJson)
            }

            // Creates a mapbox map in the ContentView
            MapboxMap(
                Modifier.fillMaxSize(),
                mapViewportState = rememberMapViewportState {
                    setCameraOptions {
                        zoom(14.0)
                        center(Point.fromLngLat(-71.41547, 41.821369))
                        pitch(0.0)
                        bearing(0.0)
                    }
                },
            ) {

                // Iterates through each feature in the FeatureCollection of the GeoJSON, and creates a marker for each.
                featureCollection?.features()?.forEach { feature ->
                    val geometry = feature.geometry()
                    if (geometry is Point) {

                        // Grabs json data from the GeoJSON file and create jsonObject or jsonArray to access the values
                        val properties = feature.properties()
                        val jsonObjectStoreName = properties?.get("name")?.asString
                        val jsonObjectAddress = properties?.get("address")?.asString
                        val jsonObjectPhoneNumber = properties?.get("phone")?.asString

                        // Creates a PointAnnotation to act as the marker for each business.
                        PointAnnotation(point = geometry) {

                            //Sets the marker image
                            iconImage = markerImage

                            // When a marker is tapped:
                            // - The data about the tapped marker is grabbed from the GeoJSON file
                            // - The data is then assigned to the global variables which overwrites the text objects in the ModalBottomSheet
                            interactionsState.onClicked {
                                locationName.value = jsonObjectStoreName.toString()
                                locationAddress.value = jsonObjectAddress.toString()
                                locationPhoneNumber.value = jsonObjectPhoneNumber.toString()
                                showBottomSheet = true
                                true
                            }
                        }
                    }
                }
            }

            // Checks if a marker has been tapped, if so will open the ModalBottomSheet
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    }
                ) {
                    // Contents of the ModalBottomSheet

                    // Aligns the contents of the ModalBottomSheet for better readability
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // These values update when the marker is selected by querying the GeoJSON
                        // for the related data, and then updating the global variables so the text prints correctly here.
                        Text(locationName.value)
                        Text(locationAddress.value)
                        Text(locationPhoneNumber.value)

                        // Adds padding at the bottom of the ModalBottomSheet
                        Spacer(modifier = Modifier.padding(vertical = 50.dp))

                    }
                }
            }
        }
    }
}
