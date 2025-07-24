package com.example.androidlocationsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex

import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.result.SearchResult

@OptIn(ExperimentalComposeUiApi::class)

class MainActivity : ComponentActivity() {

    private lateinit var placeAutocomplete: PlaceAutocomplete

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        placeAutocomplete = PlaceAutocomplete.create()

        setContent {
            val selectedResult = remember { mutableStateOf<SearchResult?>(null) }
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(-98.0, 39.5))
                    zoom(2.0)
                }
            }

            Box(Modifier.fillMaxSize()) {
                MapboxMap(
                    Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState,
                    scaleBar = {}, // disables the scale bar by rendering nothing
                    compass = {} // disable the compass
                ) {

                    selectedResult.value?.coordinate?.let { coord ->
                        val marker = rememberIconImage(
                            key = R.drawable.map_marker,
                            painter = painterResource(id = R.drawable.map_marker)
                        )
                        PointAnnotation(point = coord) {
                            iconImage = marker
                        }
                    }
                }

                SearchScreen(
                    mapViewportState = mapViewportState,
                    onSuggestionSelected = { result ->
                        selectedResult.value = result
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                )
            }
        }
    }
}