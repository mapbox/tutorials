package com.example.jetpackcomposetest

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions


@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
@Composable
fun OpenMap(
    routes:  List<NavigationRoute>?,
    mapboxNavigation: MapboxNavigation,
    //    navigationLocationProvider: NavigationLocationProvider,

) {
    var userLocation =  Point.fromLngLat(-98.23580, 39.40344) // center of U.S., random location for initialization
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(userLocation)
            zoom(3.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    val routeLineApi = MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
    val routeLineView = MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(LocalContext.current).build())

    val navigationLocationProvider = NavigationLocationProvider()

    val replayRouteMapper = ReplayRouteMapper()

    val mapView = remember { mutableStateOf<MapView?>(null) }
    val viewportDataSource = remember { mutableStateOf<MapboxNavigationViewportDataSource?>(null) }

    val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        // Jump to the current indicator position
        mapView.value?.mapboxMap?.setCamera(CameraOptions.Builder().center(it).build())
        // Set the gestures plugin's focal point to the current indicator location.
        //mapView.gestures.focalPoint = binding.mapView.mapboxMap.pixelForCoordinate(it)
    }


    LaunchedEffect(mapboxNavigation) {
        val routesObserver = RoutesObserver { routeUpdateResult ->
            if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
                routeLineApi.setNavigationRoutes(routeUpdateResult.navigationRoutes) { value ->
                    mapView.value?.mapboxMap?.getStyle()?.let { style ->
                        routeLineView.renderRouteDrawData(style, value)
                    }
                }
                viewportDataSource.value?.onRouteChanged(routeUpdateResult.navigationRoutes.first())
                viewportDataSource.value?.evaluate()
            }
        }

        val locationObserver = object : LocationObserver {

            /**
             * Provides the best possible locxation update, snapped to the route or
             * map-matched to the road if possible.
             */
            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {

                val enhancedLocation = locationMatcherResult.enhancedLocation
                navigationLocationProvider.changePosition(
                    enhancedLocation,
                    locationMatcherResult.keyPoints,
                )
                userLocation = Point.fromLngLat(enhancedLocation.longitude,enhancedLocation.latitude)

                // update viewportDataSource to trigger camera to follow the location
                viewportDataSource.value?.onLocationChanged(enhancedLocation)
                viewportDataSource.value?.evaluate()

//                if(!tripStarted)
//                {
//                    tripStarted = true
//                    startRoute()
//                }
            }

            override fun onNewRawLocation(rawLocation: com.mapbox.common.location.Location) {
            }
        }


        mapboxNavigation.registerRoutesObserver(routesObserver)
        mapboxNavigation.registerLocationObserver(locationObserver)

        val replayProgressObserver =
            ReplayProgressObserver(mapboxNavigation.mapboxReplayer, replayRouteMapper)
        mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)


    }

    LaunchedEffect(Unit) {
        routes?.let { nonNullRoutes ->
            mapboxNavigation.setNavigationRoutes(nonNullRoutes)

            // start simulated user movement COMMENT THIS OUT TO HAVE USER LOCATION ONLY
            val replayData =
                replayRouteMapper.mapDirectionsRouteGeometry(routes.first().directionsRoute)
            mapboxNavigation.mapboxReplayer.pushEvents(replayData)
            mapboxNavigation.mapboxReplayer.seekTo(replayData[0])
            mapboxNavigation.mapboxReplayer.play()

            mapboxNavigation.startReplayTripSession()
            mapViewportState.transitionToFollowPuckState()

//          mapboxNavigation.startTripSession()
        }
    }

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
                MapEffect() { map ->
                    mapView.value = map
                    map.location.apply {
                        this.locationPuck = LocationPuck2D(
                            bearingImage = ImageHolder.from(R.drawable.mapbox_user_puck_icon),
                            shadowImage = ImageHolder.from(R.drawable.mapbox_user_icon_shadow),
                            scaleExpression = interpolate {
                                linear()
                                zoom()
                                stop {
                                    literal(0.0)
                                    literal(0.6)
                                }
                                stop {
                                    literal(20.0)
                                    literal(1.0)
                                }
                            }.toJson()
                        )
                        setLocationProvider(navigationLocationProvider)
                        enabled = true
                        puckBearing = PuckBearing.COURSE
                        puckBearingEnabled = true
                    }
                    viewportDataSource.value = MapboxNavigationViewportDataSource(map.mapboxMap)


                }
                DisposableMapEffect(Unit) { map ->
                    map.location.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = true)
                        puckBearingEnabled = true
                        puckBearing = PuckBearing.HEADING
                    }

                    mapView.value?.location?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)

                    onDispose {
//                        map.location.removeOnIndicatorPositionChangedListener(locationListener)
                    }
                }
            }
        }
    }
}
