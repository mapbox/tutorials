package com.example.jetpackcomposetest

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
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
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.utils.DecodeUtils.completeGeometryToLineString
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
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

public class MainActivity : ComponentActivity() {

    private lateinit var replayProgressObserver: ReplayProgressObserver

    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(routeUpdateResult.navigationRoutes) { value ->
                mapView?.mapboxMap?.style?.apply { routeLineView.renderRouteDrawData(this, value) }
            }

            // update viewportSourceData to include the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource.evaluate()
        }
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)

                replayProgressObserver =
                    ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
            }
        },
        onInitialize = this::initNavigation
    )
    private val replayRouteMapper = ReplayRouteMapper()

    private var mapView : MapView? = null

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        // Jump to the current indicator position
        mapView?.mapboxMap?.setCamera(CameraOptions.Builder().center(it).build())
        // Set the gestures plugin's focal point to the current indicator location.
        //mapView.gestures.focalPoint = binding.mapView.mapboxMap.pixelForCoordinate(it)
    }

    private lateinit var mapViewportState : MapViewportState

    private var userLocation =  Point.fromLngLat(-71.33044, 41.99054)

    private var destination = Point.fromLngLat(0.0, 0.0)  //random location for initialization

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .build()
        )
    }

    private val navigationLocationProvider = NavigationLocationProvider()

    private var routeLine: LineString? = null

    private var tripStarted = false

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver = object : LocationObserver {

        /**
         * Provides the best possible location update, snapped to the route or
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
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            if(!tripStarted)
            {
                tripStarted = true
                startRoute()
            }
        }

        override fun onNewRawLocation(rawLocation: com.mapbox.common.location.Location) {
        }
    }

    @Composable
    @SuppressLint("MissingPermission")
    private fun OpenMap()
    {
            MapboxMapComposeTheme {
                ExampleScaffold(
                    floatingActionButton = {
                        Column{
                            FloatingActionButton(
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .align(Alignment.End),
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
                                    Text(modifier = Modifier.padding(10.dp), text = "Overview")
                                } else {
                                    Text(modifier = Modifier.padding(10.dp), text = "Follow puck")
                                }
                            }
                        }

                    }
                ) {

                    routeLineApi = MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
                    routeLineView = MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(this).build())

                    MapboxMap(
                        Modifier.fillMaxSize(),
                        mapViewportState = mapViewportState
                    ) {

                        MapEffect() { map ->

                            mapView = map
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
                            viewportDataSource = MapboxNavigationViewportDataSource(map.mapboxMap)
                        }
                        DisposableMapEffect(Unit) { map ->
                            map.location.updateSettings {
                                locationPuck = createDefault2DPuck(withBearing = true)
                                puckBearingEnabled = true
                                puckBearing = PuckBearing.HEADING
                            }
                            val locationListener = OnIndicatorPositionChangedListener {}
                            map.location.addOnIndicatorPositionChangedListener(locationListener)
                            onDispose {
                                map.location.removeOnIndicatorPositionChangedListener(locationListener)
                            }
                        }

                        mapViewportState.transitionToFollowPuckState()

                        mapboxNavigation.startTripSession()
                    }

                }
            }

    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(userLocation)
                    zoom(10.0)
                    pitch(0.0)
                    bearing(0.0)
                }
            }

            RenderButtons()
        }
    }

    @Composable
    private fun RenderButtons()
    {
            var startRouteToSFBool by remember { mutableStateOf(false) }
            var startRouteToDCBool by remember { mutableStateOf(false) }

        Column(
                Modifier.fillMaxWidth().fillMaxHeight()
            ) {
                if (startRouteToSFBool) {
                    destination = Point.fromLngLat(-122.413683, 37.775707)
                    OpenMap()
                }
                if (startRouteToDCBool) {
                    destination = Point.fromLngLat(-77.034065, 38.904856)
                    OpenMap()
                }
                FloatingActionButton(
                    modifier = Modifier.padding(top = 300.dp,bottom = 10.dp).align(Alignment.CenterHorizontally),
                    onClick = { startRouteToSFBool = true },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(modifier = Modifier.padding(10.dp), text = "Start Route to Mapbox SF HQ")
                }
                FloatingActionButton(
                    modifier = Modifier.padding(all = 10.dp).align(Alignment.CenterHorizontally),
                    onClick = { startRouteToDCBool = true },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(modifier = Modifier.padding(10.dp), text = "Start Route to Mapbox DC HQ")
                }
            }
    }

    override fun onStart() {
        super.onStart()
        mapView?.location
            ?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    override fun onStop() {
        super.onStop()
        mapView?.location
            ?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun startRoute()
    {
            val routeOptions = RouteOptions.builder()
                // applies the default parameters to route options
                .applyDefaultNavigationOptions()
                // lists the coordinate pair i.e. origin and destination
                // If you want to specify waypoints you can pass list of points instead of null
                .coordinatesList(listOf(userLocation, destination))
                // set it to true if you want to receive alternate routes to your destination
                .alternatives(false)
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                // provide the bearing for the origin of the request to ensure
                // that the returned route faces in the direction of the current user movement
                .bearingsList(
                    listOf(
                        Bearing.builder()
                            .angle(45.0)
                            .degrees(45.0)
                            .build(),
                        null
                    )
                )
                .build()

            mapboxNavigation.requestRoutes(
                routeOptions,
                object : NavigationRouterCallback {
                    override fun onCanceled(
                        routeOptions: RouteOptions,
                        routerOrigin: String
                    ) {
                    }

                    override fun onFailure(
                        reasons: List<RouterFailure>,
                        routeOptions: RouteOptions
                    ) {
                    }

                    @SuppressLint("MissingPermission")
                    override fun onRoutesReady(
                        routes: List<NavigationRoute>,
                        routerOrigin: String
                    ) {
                        routeLine = routes.first().directionsRoute.completeGeometryToLineString()
                                .also {
                                    // immediately transition to overview viewport state after route line is available
                                    mapViewportState.transitionToOverviewState(
                                        OverviewViewportStateOptions.Builder().geometry(it)
                                            .padding(EdgeInsets(50.0, 50.0, 50.0, 50.0))
                                            .build()
                                    )
                                }

                        mapView?.mapboxMap?.getStyle { style ->
                            // Specify a unique string as the source ID (SOURCE_ID)
                            // and reference the location of source data
                            style.addSource(
                                geoJsonSource("routeData") {
                                   geometry(routeLine!!)
                                }
                            )

                            // Specify a unique string as the layer ID (LAYER_ID)
                            // and reference the source ID (SOURCE_ID) added above.
                            style.addLayer(
                                lineLayer("routeLine", "routeData") {
                                    lineColor(Color.BLUE)
                                    lineWidth(5.0)
                                    slot("top")
                                }
                            )
                        }

                        mapboxNavigation.setNavigationRoutes(routes)

                        // start simulated user movement
                        /*val replayData =
                            replayRouteMapper.mapDirectionsRouteGeometry(routes.first().directionsRoute)
                        mapboxNavigation.mapboxReplayer.pushEvents(replayData)
                        mapboxNavigation.mapboxReplayer.seekTo(replayData[0])
                        mapboxNavigation.mapboxReplayer.play()

                        mapboxNavigation.startReplayTripSession()*/
                        mapboxNavigation.startTripSession()

                    }
                }
            )

    }
}