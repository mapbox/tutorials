package com.example.androidlocationsearch

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    mapViewportState: MapViewportState,
    modifier: Modifier = Modifier,
    onSuggestionSelected: (SearchResult) -> Unit
) {
    val context = LocalContext.current;
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<SearchSuggestion>>(emptyList()) }

    val locationProvider = LocationServiceFactory.getOrCreate()
        .getDeviceLocationProvider(null)
        .value

    val searchEngineSettings =   SearchEngineSettings(
        locationProvider = locationProvider
    )

    // Get SearchEngine instance
    val searchEngine = remember {
        SearchEngine.createSearchEngine(
            ApiType.SEARCH_BOX,
            searchEngineSettings
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Inline SearchInput UI
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                if (newQuery.length >= 2) {
                    searchEngine.search(
                        newQuery,
                        SearchOptions(
                            //proximity = Point.fromLngLat(-79.35954, 43.65050), // Proximity to Toronto's Distillery District
                            limit = 10,
                            // boundingBox = BoundingBox.fromPoints(
                               // Point.fromLngLat(-79.49555, 43.60698),
                               // Point.fromLngLat(-79.29422, 43.75953)
                            // ) // Bounding Box of the Greater Toronto Area
                        ),
                        callback = object : com.mapbox.search.SearchSuggestionsCallback {
                            override fun onSuggestions(
                                list: List<SearchSuggestion>,
                                responseInfo: ResponseInfo
                            ) {
                                suggestions = list
                            }

                            override fun onError(e: Exception) {
                                Log.e("SearchScreen", "Search error", e)
                            }
                        }
                    )
                } else {
                    suggestions = emptyList()
                }
            },

            label = { Text("Search") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.White,          // White background
                focusedBorderColor = Color.Gray,       // Grey border when focused
                unfocusedBorderColor = Color.LightGray // Grey border when unfocused (lighter)
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Suggestions list anchored right below the TextField
        if (suggestions.isNotEmpty()) {
            Surface(
                color = Color.White,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .heightIn(max = 500.dp)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    suggestions.forEachIndexed { index, suggestion ->

                        val distanceKm = suggestion.distanceMeters?.div(1000.0)
                        val addressText = suggestion.fullAddress
                            ?: listOfNotNull(suggestion.address?.region, suggestion.address?.country).joinToString(", ")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    suggestions = emptyList()
                                    handleSuggestionSelection(suggestion, searchEngine, mapViewportState, onSuggestionSelected)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.search_result_marker),
                                contentDescription = "Mapbox Marker",
                                tint = Color.Unspecified, // Use this if your vector defines its own fill color
                                modifier = Modifier
                                    .size(24.dp)
                            )

                            Column(
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Text(
                                    text = suggestion.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                Text(
                                    text = addressText,
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                distanceKm?.let { km ->
                                    Text(
                                        text = String.format("%.1f km", km),
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                            }
                        }
                        if (index < suggestions.lastIndex) {
                            Divider(color = Color.LightGray)
                        }
                    }

                }
            }
        }
    }
}

fun handleSuggestionSelection(
    suggestion: SearchSuggestion,
    searchEngine: SearchEngine,
    mapViewportState: MapViewportState,
    onSuggestionSelected: (SearchResult) -> Unit

) {
    searchEngine.select(suggestion, object: SearchSelectionCallback {
        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            // When user selects a suggestion:
            onSuggestionSelected(result)
            val coordinate = result.coordinate
            if (coordinate != null) {
                val camera = cameraOptions {
                    center(coordinate)
                    zoom(14.0)
                }

                val animationOptions = MapAnimationOptions.Builder()
                    .duration(3000L) // 3 seconds
                    .build()

                mapViewportState.flyTo(camera, animationOptions)
            } else {
                Log.w("Search", "No coordinate found for result.")
            }
        }

        override fun onResults(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            // handle multiple results (category, brand, etc.)
        }


        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            // override if needed
        }

        override fun onError(e: Exception) {
            Log.e("Search", "Selection error", e)
        }
    })
}


