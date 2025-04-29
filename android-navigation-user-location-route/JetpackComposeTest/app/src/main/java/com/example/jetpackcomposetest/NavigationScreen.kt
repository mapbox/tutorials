package com.example.jetpackcomposetest

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.ViewportStatus
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import com.mapbox.maps.plugin.viewport.state.FollowPuckViewportState
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.utils.DecodeUtils.completeGeometryToLineString
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState


@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
@Composable
@SuppressLint("MissingPermission")
fun NavigationScreen(
    startLocation: Point,
    destination: Point,
    mapboxNavigation: MapboxNavigation,
    viewportDataSource: MapboxNavigationViewportDataSource,
    navigationLocationProvider: NavigationLocationProvider,
    routeLineApi: MapboxRouteLineApi,
    routeLineView: MapboxRouteLineView,
    replayRouteMapper: ReplayRouteMapper
) {
    var mapView: MapView? by remember { mutableStateOf(null) }
    var tripStarted by remember { mutableStateOf(false) }
    var routeLine: LineString? by remember { mutableStateOf(null) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(startLocation)
            zoom(3.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    ExampleScaffold(
        floatingActionButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    modifier = Modifier.padding(end = 16.dp),
                    onClick = {
                        if ((mapViewportState.mapViewportStatus as? ViewportStatus.State)?.state is FollowPuckViewportState) {
                            routeLine?.let {
                                mapViewportState.transitionToOverviewState(
                                    overviewViewportStateOptions = OverviewViewportStateOptions.Builder()
                                        .geometry(it)
                                        .padding(EdgeInsets(50.0, 50.0, 50.0, 50.0))
                                        .build()
                                )
                            }
                        } else {
                            mapViewportState.transitionToFollowPuckState()
                        }
                    }
                ) {
                    if ((mapViewportState.mapViewportStatus as? ViewportStatus.State)?.state is FollowPuckViewportState) {
                        Text("Overview")
                    } else {
                        Text("Follow puck")
                    }
                }
            }
        }
    ) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState
        ) {
            MapEffect { map ->
                mapView = map

                map.location.apply {
                    locationPuck = LocationPuck2D(
                        bearingImage = ImageHolder.from(R.drawable.mapbox_user_puck_icon),
                        shadowImage = ImageHolder.from(R.drawable.mapbox_user_icon_shadow),
                        scaleExpression = interpolate {
                            linear()
                            zoom()
                            stop { literal(0.0); literal(0.6) }
                            stop { literal(20.0); literal(1.0) }
                        }.toJson()
                    )
                    setLocationProvider(navigationLocationProvider)
                    enabled = true
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                }

//                // Now bind viewportDataSource
//                viewportDataSource.mapboxMap = map.mapboxMap
            }

            DisposableMapEffect(Unit) { map ->
                map.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    puckBearingEnabled = true
                    puckBearing = PuckBearing.HEADING
                }

                val listener = OnIndicatorPositionChangedListener { point ->
                    map.mapboxMap.setCamera(CameraOptions.Builder().center(point).build())
                }
                map.location.addOnIndicatorPositionChangedListener(listener)

                onDispose {
                    map.location.removeOnIndicatorPositionChangedListener(listener)
                }
            }

            if (!tripStarted) {
                LaunchedEffect(Unit) {
                    tripStarted = true
                    startRoute(
                        startLocation,
                        destination,
                        mapboxNavigation,
                        routeLineApi,
                        routeLineView,
                        replayRouteMapper
                    ) { geometry ->
                        routeLine = geometry
                        mapViewportState.transitionToFollowPuckState()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
@SuppressLint("MissingPermission")
private fun startRoute(
    start: Point,
    destination: Point,
    mapboxNavigation: MapboxNavigation,
    routeLineApi: MapboxRouteLineApi,
    routeLineView: MapboxRouteLineView,
    replayRouteMapper: ReplayRouteMapper,
    onRouteReady: (LineString) -> Unit
) {
    val options = RouteOptions.builder()
        .applyDefaultNavigationOptions()
        .coordinatesList(listOf(start, destination))
        .alternatives(false)
        .bearingsList(
            listOf(Bearing.builder().angle(45.0).degrees(45.0).build(), null)
        )
        .build()

    mapboxNavigation.requestRoutes(
        options,
        object : NavigationRouterCallback {
            override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                mapboxNavigation.setNavigationRoutes(routes)

                val replayData = replayRouteMapper.mapDirectionsRouteGeometry(routes.first().directionsRoute)
                mapboxNavigation.mapboxReplayer.pushEvents(replayData)
                mapboxNavigation.mapboxReplayer.seekTo(replayData[0])
                mapboxNavigation.mapboxReplayer.play()
                mapboxNavigation.startReplayTripSession()

                val geometry = routes.first().directionsRoute.completeGeometryToLineString()
                onRouteReady(geometry)
            }

            override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {}
            override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {}
        }
    )
}
