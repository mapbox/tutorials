package com.example.interactions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.style.standard.LightPresetValue
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle
import com.mapbox.maps.extension.compose.style.standard.rememberStandardStyleState
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.mapbox.maps.extension.compose.style.imports.rememberStyleImportState
import com.mapbox.maps.interactions.standard.generated.StandardPlaceLabelsFeature
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.mapbox.maps.interactions.FeatureState
import com.mapbox.maps.interactions.FeaturesetFeature
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import androidx.compose.foundation.layout.Box
import com.mapbox.maps.viewannotation.geometry
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val selectedPlaces = remember {
                mutableStateListOf<StandardPlaceLabelsFeature>()
            }

            var selectedPriceLabel by remember {
                mutableStateOf<FeaturesetFeature<FeatureState>?>(null)
            }

            MapboxMap(
                Modifier.fillMaxSize().padding(top = 20.dp),
                mapViewportState = rememberMapViewportState {
                    setCameraOptions(
                        cameraOptions {
                            center(Point.fromLngLat(-73.99, 40.72))
                            zoom(11.0)
                            pitch(45.0)
                        }
                    )
                },
                style = {
                    MapboxStandardStyle(
                        standardStyleState = rememberStandardStyleState {
                            configurationsState.apply {
                                lightPreset = LightPresetValue.DAWN
                            }
                            interactionsState.onPlaceLabelsClicked { placeLabel, _ ->
                                placeLabel.setStandardPlaceLabelsState {
                                    select(select = true)
                                }
                                selectedPlaces.add(placeLabel)
                                return@onPlaceLabelsClicked true
                            }
                            interactionsState.onMapLongClicked { _ ->
                                selectedPlaces.forEach {
                                    it.removeFeatureState()
                                }
                                return@onMapLongClicked true
                            }
                        },
                        styleImportsContent = {
                            StyleImport(
                                importId = "new-york-hotels",
                                style = "asset://new-york-hotels.json",
                                styleImportState = rememberStyleImportState {
                                    interactionsState.onFeaturesetClicked("hotels-price") { priceLabel, _ ->
                                        if (selectedPriceLabel?.id != priceLabel.id) {
                                            selectedPriceLabel = priceLabel
                                            selectedPriceLabel?.setFeatureState(
                                                FeatureState {
                                                    addBooleanState("hidden", true)
                                                }
                                            )
                                        }
                                        return@onFeaturesetClicked true
                                    }
                                }
                            )
                        }
                    )
                }
            ) {
                selectedPriceLabel?.let {
                    ViewAnnotation(
                        options = viewAnnotationOptions {
                            // Fallback to the center of the map if the geometry is null
                            geometry(selectedPriceLabel?.geometry ?: Point.fromLngLat(-73.99, 40.72))
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 8.dp,
                                )
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "${selectedPriceLabel?.properties?.getString("name")}",
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "$${selectedPriceLabel?.properties?.getString("price")}",
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}