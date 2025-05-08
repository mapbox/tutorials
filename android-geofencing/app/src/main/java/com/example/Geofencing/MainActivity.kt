package com.example.Geofencing

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.gson.JsonPrimitive
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.common.geofencing.GeofencingError
import com.mapbox.common.geofencing.GeofencingEvent
import com.mapbox.common.geofencing.GeofencingFactory
import com.mapbox.common.geofencing.GeofencingObserver
import com.mapbox.common.geofencing.GeofencingPropertiesKeys
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.layers.generated.FillLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.match
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(MapboxExperimental::class)
public class MainActivity : ComponentActivity() {
    private val geofencing by lazy {
        GeofencingFactory.getOrCreate()
    }
    val lastEvent: MutableState<GeofenceEvent?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    zoom(9.0)
                    center(Point.fromLngLat(-110.6123, 44.5979))
                    pitch(0.0)
                    bearing(0.0)
                }
            }
            MapboxMap(
                Modifier.fillMaxSize().padding(top = 20.dp),
                mapViewportState = mapViewportState
            ) {
                FillLayer(
                    sourceState = rememberGeoJsonSourceState {
                        data = GeoJSONData("asset://yellowstone.geojson")
                    }
                ) {
                    fillColor = ColorValue(
                        match {
                            id()
                            literal(lastEvent.value?.feature?.id().toString())
                            match {
                                literal(lastEvent.value?.type?.description.toString())
                                stop {
                                    literal("entry")
                                    rgb(7.0, 144.0, 30.0) // green
                                }
                                stop {
                                    literal("exit")
                                    rgb(173.0, 17.0, 5.0) // red
                                }
                                stop {
                                    literal("dwell")
                                    rgb(17.0, 97.0, 195.0) // blue
                                }
                                rgb(119.0, 119.0, 119.0) // gray
                            }
                            rgb(119.0, 119.0, 119.0) // gray
                        }
                    )
                    fillOpacity = DoubleValue(0.7)
                }
                MapEffect(Unit) { mapView ->
                    mapView.location.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = true)
                        enabled = true
                    }
                }
            }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
            ) { permissionsMap ->
                val granted = permissionsMap.values.all { it }
                if (granted) {
                    startGeofencing()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "You need to accept location permissions for geofencing to function.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            LaunchedEffect(Unit) {
                if (locationPermissions.all {
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            it
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                ) {
                    startGeofencing()
                } else {
                    launcher.launch(locationPermissions)
                }
            }
            lastEvent.value?.let {
                Toast.makeText(
                    this@MainActivity,
                    "${it.type.formatted} ${it.geofenceName} at ${it.timestamp}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val observer: GeofencingObserver = object : GeofencingObserver {
        override fun onEntry(event: GeofencingEvent) {
            lastEvent.value = GeofenceEvent(GeofenceEvent.GeofenceEventType.ENTRY, event)
        }

        override fun onDwell(event: GeofencingEvent) {
            lastEvent.value = GeofenceEvent(GeofenceEvent.GeofenceEventType.DWELL, event)
        }

        override fun onExit(event: GeofencingEvent) {
            lastEvent.value = GeofenceEvent(GeofenceEvent.GeofenceEventType.EXIT, event)
        }

        override fun onError(error: GeofencingError) {
            Log.d("YellowstoneApp", "onError() called with: error = $error")
        }

        override fun onUserConsentChanged(isConsentGiven: Boolean) {
            Log.d("YellowstoneApp", "onUserConsentChanged() called with: isConsentGiven = $isConsentGiven")
        }
    }

    private fun startGeofencing() {
        Log.d("YellowstoneApp", "startGeofencing")
        /// Geofences are stored in database on disk.
        /// To make this example isolated and synchronized with the UI we delete existing feature from database.
        geofencing.clearFeatures {
            geofencing.addObserver(observer) { geofenceError ->
                Log.d("YellowstoneApp", "geofence.addObserver() error $geofenceError")
            }
        }
        decodeGeoJSON(this@MainActivity, "yellowstone.geojson")?.let { featureCollection ->
            featureCollection.features()?.forEach { feature ->
                // To receive dwell events we need to set a time.
                // After a user has spent that amount of time in the geofence
                // the Geofencing service will send a dwell event
                feature.addProperty(GeofencingPropertiesKeys.DWELL_TIME_KEY, JsonPrimitive(1)) // minutes
                geofencing.addFeature(feature) {}
            }
        }
    }
}

@OptIn(MapboxExperimental::class)
data class GeofenceEvent(
    val type: GeofenceEventType,
    val feature: Feature,
    val geofenceName: String,
    val timestamp: String
) {
    enum class GeofenceEventType(val description: String, val formatted: String) {
        ENTRY("entry", "Entered"),
        DWELL("dwell", "Dwelled in"),
        EXIT("exit", "Exited")
    }

    constructor(type: GeofenceEventType, event: GeofencingEvent) : this(
        type = type,
        feature = event.feature,
        geofenceName = event.feature.getStringProperty("name") ?: "unknown geofence",
        timestamp = event.timestamp.format()
    )

    companion object {
        private fun Date.format(): String {
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault()) // 12-hour format with AM/PM
            return formatter.format(this)
        }
    }
}

fun decodeGeoJSON(context: Context, fileName: String): FeatureCollection? = try {
    val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
    FeatureCollection.fromJson(json)
} catch (e: IOException) {
    Log.e("YellowstoneApp", "Unable to parse $fileName")
    null
}

private val locationPermissions = arrayOf(
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACCESS_COARSE_LOCATION
)