package com.example.jetpackcomposetest

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.ViewportStatus
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import com.mapbox.maps.plugin.viewport.state.FollowPuckViewportState
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView

@SuppressLint("MissingPermission")
@Composable
fun OpenMap(
    mapViewportState: MapViewportState,
    mapboxNavigation: MapboxNavigation,
    //    navigationLocationProvider: NavigationLocationProvider,

//    routeLine: RouteLine?,
//    routeLineApi: MapboxRouteLineApi,
//    routeLineView: MapboxRouteLineView,
//    viewportDataSource: MapboxNavigationViewportDataSource,
//    mapView: MapView
) {
    MapboxMapComposeTheme {
        ExampleScaffold(
            floatingActionButton = {
                Column {
//                    FloatingActionButton(
//                        modifier = Modifier
//                            .padding(bottom = 10.dp)
//                            .align(Alignment.End),
//                        onClick = {
//                            if ((mapViewportState.mapViewportStatus as? ViewportStatus.State)?.state is FollowPuckViewportState) {
//                                routeLine?.let {
//                                    mapViewportState.transitionToOverviewState(
//                                        overviewViewportStateOptions = OverviewViewportStateOptions.Builder()
//                                            .geometry(it)
//                                            .padding(EdgeInsets(50.0, 50.0, 50.0, 50.0))
//                                            .build()
//                                    )
//                                }
//                            } else {
//                                mapViewportState.transitionToFollowPuckState()
//                            }
//                        }
//                    ) {
//                        if ((mapViewportState.mapViewportStatus as? ViewportStatus.State)?.state is FollowPuckViewportState) {
//                            Text(modifier = Modifier.padding(10.dp), text = "Overview")
//                        } else {
//                            Text(modifier = Modifier.padding(10.dp), text = "Follow puck")
//                        }
//                    }
                }
            }
        ) {
            MapboxMap(
                Modifier.fillMaxSize(),
                mapViewportState = mapViewportState
            ) {
//                MapEffect() { map ->
//                    mapView.location.apply {
//                        this.locationPuck = LocationPuck2D(
//                            bearingImage = ImageHolder.from(R.drawable.mapbox_user_puck_icon),
//                            shadowImage = ImageHolder.from(R.drawable.mapbox_user_icon_shadow),
//                            scaleExpression = interpolate {
//                                linear()
//                                zoom()
//                                stop {
//                                    literal(0.0)
//                                    literal(0.6)
//                                }
//                                stop {
//                                    literal(20.0)
//                                    literal(1.0)
//                                }
//                            }.toJson()
//                        )
//                        setLocationProvider(navigationLocationProvider)
//                        enabled = true
//                        puckBearing = PuckBearing.COURSE
//                        puckBearingEnabled = true
//                    }
//                    viewportDataSource = MapboxNavigationViewportDataSource(map.mapboxMap)
//                }
//                DisposableMapEffect(Unit) { map ->
//                    map.location.updateSettings {
//                        locationPuck = createDefault2DPuck(withBearing = true)
//                        puckBearingEnabled = true
//                        puckBearing = PuckBearing.HEADING
//                    }
//                    val locationListener = OnIndicatorPositionChangedListener {}
//                    map.location.addOnIndicatorPositionChangedListener(locationListener)
//                    onDispose {
//                        map.location.removeOnIndicatorPositionChangedListener(locationListener)
//                    }
//                }
                mapboxNavigation.startTripSession()
            }
        }
    }
}
