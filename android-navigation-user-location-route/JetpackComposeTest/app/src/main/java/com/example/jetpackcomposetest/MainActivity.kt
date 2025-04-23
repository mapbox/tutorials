package com.example.jetpackcomposetest

import android.R.style
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.MapboxStyleComposable
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.compose.style.standard.LightPresetValue
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.utils.DecodeUtils.completeGeometryToLineString
import com.mapbox.navigation.base.utils.DecodeUtils.completeGeometryToPoints
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import java.lang.ref.WeakReference


/**
 * Example to showcase usage of runtime styling with compose.
 */

public class MainActivity : ComponentActivity() {

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerLocationObserver(locationObserver)
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterLocationObserver(locationObserver)
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

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private var userLocation =  Point.fromLngLat(-71.33044, 41.99054)

    private var destination = Point.fromLngLat(-71.33189, 41.98031)

    private var progress = 0.0

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .build()
        )
    }

    private val navigationLocationProvider = NavigationLocationProvider()

    private var lightPreset: LightPresetValue = LightPresetValue.DAY

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

            //Log.e("EKR", locationMatcherResult.enhancedLocation.toString())

            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
            userLocation = Point.fromLngLat(enhancedLocation.longitude,enhancedLocation.latitude)

            if(!tripStarted)
            {
                tripStarted = true
                StartRoute()
            }
        }

        override fun onNewRawLocation(rawLocation: com.mapbox.common.location.Location) {
        }
    }

    @Composable
    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun OpenMap()
    {
        var permissionGranted = false
        locationPermissionHelper.checkPermissions {
            permissionGranted = true
        }
        if(permissionGranted)
        {
            MapboxMap(
                Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                style = {
                    NavigationStyle(routeLine = routeLine, progress = progress, lightPreset = lightPreset)
                }
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
                }
                mapViewportState.transitionToFollowPuckState()

                mapboxNavigation.startTripSession(false)

                /*Column{
                if (routeLine != null) {
                    FloatingActionButton(
                        modifier = Modifier.padding(bottom = 10.dp),
                        onClick = {
                            lightPreset = if (lightPreset == LightPresetValue.DAY) {
                                LightPresetValue.NIGHT
                            } else {
                                LightPresetValue.DAY
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(modifier = Modifier.padding(10.dp), text = "Toggle light preset")
                    }
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
                        /*if ((mapViewportState.mapViewportStatus as? ViewportStatus.State)?.state is FollowPuckViewportState) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_menu_directions),
                                contentDescription = "Overview button"
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_menu_mylocation),
                                contentDescription = "Follow puck button"
                            )
                        }*/
                    }
                }
            }*/

            }
            /*DisposableMapEffect(Unit) { map ->
            map.location.updateSettings {
                locationPuck = createDefault2DPuck(withBearing = true)
                puckBearingEnabled = true
                puckBearing = PuckBearing.HEADING
            }
            val locationListener = OnIndicatorPositionChangedListener { point ->
                // in SimulateRouteLocationProvider we use altitude field to insert animated progress info.
                progress = point.altitude()
            }
            map.location.addOnIndicatorPositionChangedListener(locationListener)
            onDispose {
                map.location.removeOnIndicatorPositionChangedListener(locationListener)
            }
        }*/
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
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

            locationPermissionHelper = LocationPermissionHelper(WeakReference(this))

            var startRouteToSFBool by remember { mutableStateOf(false) }
            var startRouteToDCBool by remember { mutableStateOf(false) }

            MapboxMapComposeTheme {
                ExampleScaffold(
                    floatingActionButton = {
                        Column {
                            if (startRouteToSFBool) {
                                destination = Point.fromLngLat(-122.413683,37.775707)
                                OpenMap()
                            }
                            if (startRouteToDCBool)
                            {
                                destination = Point.fromLngLat(-77.034065,38.904856)
                                OpenMap()
                            }
                            FloatingActionButton(
                                modifier = Modifier.padding(bottom = 10.dp),
                                onClick = { startRouteToSFBool = true },
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Text(modifier = Modifier.padding(10.dp), text = "Start Route to Mapbox SF HQ")
                            }
                            FloatingActionButton(
                                modifier = Modifier.padding(bottom = 10.dp),
                                onClick = { startRouteToDCBool = true },
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Text(modifier = Modifier.padding(10.dp), text = "Start Route to Mapbox DC HQ")
                            }
                        }
                    }
                ) {

                    }
                }
            }
        }

    @MapboxStyleComposable
    @Composable
    public fun NavigationStyle(
        routeLine: LineString?,
        progress: Double,
        lightPreset: LightPresetValue
    ) {
        val geoJsonSource = rememberGeoJsonSourceState {
            lineMetrics = BooleanValue(true)
        }
        LaunchedEffect(routeLine) {
            routeLine?.let {
                geoJsonSource.data = GeoJSONData(it)
            }
        }
        MapboxStandardStyle(
            topSlot = {
                if (routeLine != null) {

                }
            }
        ) {
            this.lightPreset = lightPreset
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
    private fun StartRoute()
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

                    override fun onRoutesReady(
                        routes: List<NavigationRoute>,
                        routerOrigin: String
                    ) {
                        mapboxNavigation.setNavigationRoutes(routes)

                        routeLine =
                            LineString.fromLngLats(routes.first().directionsRoute.completeGeometryToPoints())//routes.first().directionsRoute.completeGeometryToPoints())//(routeOptions., Constants.PRECISION_6)
                                .also {
                                    // immediately transition to overview viewport state after route line is available
                                    mapViewportState.transitionToOverviewState(
                                        OverviewViewportStateOptions.Builder().geometry(it)
                                            .padding(EdgeInsets(50.0, 50.0, 50.0, 50.0))
                                            .build()
                                    )
                                }

                        /*mapView?.mapboxMap?.style?.addSource(
                            GeoJsonSource(
                                "line-source",
                                FeatureCollection.fromFeatures(
                                    arrayOf<Feature>(
                                        Feature.fromGeometry(routes.first().directionsRoute.completeGeometryToPoints())
                                        )
                                    )
                                )
                            )
                        )*/

                        // start simulated user movement
                        val replayData =
                            replayRouteMapper.mapDirectionsRouteGeometry(routes.first().directionsRoute)
                        mapboxNavigation.mapboxReplayer.pushEvents(replayData)
                        mapboxNavigation.mapboxReplayer.seekTo(replayData[0])
                        mapboxNavigation.mapboxReplayer.play()

                    }
                }
            )

    }
}