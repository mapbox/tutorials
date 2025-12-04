package com.example.android_custom_style

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.rememberStyleState
import org.json.JSONObject
import kotlin.math.roundToInt

public class MainActivity : ComponentActivity() {

    val locationName = mutableStateOf("none")
    val locationAddress = mutableStateOf("none")
    val locationPhoneNumber = mutableStateOf("none")
    val locationRating = mutableStateOf("none")

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sheetState = rememberModalBottomSheetState()
            var showBottomSheet by remember { mutableStateOf(false) }

            fun grabPOIData(properties: JSONObject)
            {
                locationName.value  = properties.get("storeName").toString()
                locationAddress.value = properties.get("address").toString() + ", " + properties.get("city").toString() + " " + properties.get("postalCode").toString()
                locationPhoneNumber.value = properties.get("phoneFormatted").toString()
                locationRating.value = properties.get("rating").toString()

                showBottomSheet = true
            }

            MapboxMap(
                Modifier.fillMaxSize(),
                scaleBar = {},
                style = {
                    MapStyle(
                        style = "mapbox://styles/examples/cm37hh1nx017n01qk2hngebzt",
                        styleState = rememberStyleState {
                            styleInteractionsState
                                .onLayerClicked(id = "dog-groomers-3o4sdb") { feature, context ->
                                    grabPOIData(feature.properties)
                                    true
                                }
                                .onLayerClicked(id = "dog-groomers-boston-marker") { feature, context ->
                                    grabPOIData(feature.properties)
                                    true
                                }
                        }
                    )
                },
                mapViewportState = rememberMapViewportState {
                    setCameraOptions {
                        zoom(7.0)
                        center(Point.fromLngLat(-71.09290,42.34622))
                        pitch(0.0)
                        bearing(0.0)
                    }
                },
            )

            // Adds UI element with call to action and app title text
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(0.15f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceEvenly)
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.3f)
                        .clip(shape = RoundedCornerShape(5.dp))
                        .padding(1.dp)
                        .background(Color.White)
                )
                {
                    Row(modifier = Modifier.padding(1.dp))
                    {
                        Icon(Icons.Filled.Favorite, "", tint = Color.Red, modifier = Modifier.padding(2.dp))
                        Text("Pet Spa Finder", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp,1.dp,1.dp,1.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f)
                        .padding(1.dp)
                        .clip(shape = RoundedCornerShape(5.dp))
                        .background(Color.White)
                )
                {
                    Row(modifier = Modifier.padding(1.dp))
                    {
                        Text("Click a marker for more information.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(5.dp,2.dp))
                    }
                }
            }

            // Checks if a marker has been tapped, if so will open the ModalBottomSheet
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState
                ) {
                    // Aligns the contents of the ModalBottomSheet for better readability
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(25.dp,0.dp,0.dp,0.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // These values update when the marker is selected by querying the GeoJSON
                        // for the related data, and then updating the global variables so the text prints correctly here.
                        Text(
                            text = locationName.value,
                            style = MaterialTheme.typography.displaySmall,
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight(0.04f))
                        {
                            Text("Rating: ", style = MaterialTheme.typography.bodyMedium)
                            val roundedRating = locationRating.value.toFloat().roundToInt()
                            for (i in 0..roundedRating-1){//locationRating.value.toInt()) {
                                Icon(Icons.Filled.Star,"",tint = Color.Red)
                            }
                        }

                        Spacer(modifier = Modifier.padding(vertical = 10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight(0.04f))
                        {
                            Icon(Icons.Filled.Place, "", tint = Color.Red, modifier = Modifier.padding(2.dp))
                            Text(locationAddress.value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(1.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxHeight(0.04f))
                        {
                            Icon(Icons.Filled.Phone, "", tint = Color.Red, modifier = Modifier.padding(3.dp))
                            Text(
                                locationPhoneNumber.value,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        // Adds padding at the bottom of the ModalBottomSheet
                        Spacer(modifier = Modifier.padding(vertical = 50.dp))

                    }

                }
            }

        }
    }

}