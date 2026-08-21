package com.example.nav_turn_tutorial

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.geojson.Point
import kotlinx.coroutines.launch

private const val TAG = "DestinationList"

@Composable
fun DestinationListScreen(
    modifier: Modifier = Modifier,
    onNavigationReady: (PreparedNavigation) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navigationLoader = remember { NavigationLoader(context) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isLoadingRoute by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasLocationPermission = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    fun requestRouteTo(destination: Destination) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        isLoadingRoute = true
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location == null) {
                Log.w(TAG, "Current location unavailable")
                isLoadingRoute = false
                return@addOnSuccessListener
            }
            val origin = Point.fromLngLat(location.longitude, location.latitude)
            Log.d(TAG, "Origin: $origin, Destination: ${destination.point}")
            scope.launch {
                val prepared = navigationLoader.loadNavigation(origin, destination.point)
                Log.d(TAG, "Route distance: ${prepared.distanceMeters} meters")
                isLoadingRoute = false
                onNavigationReady(prepared)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Choose a destination")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(destinations) { destination ->
                Button(
                    onClick = { requestRouteTo(destination) },
                    enabled = !isLoadingRoute
                ) {
                    Text(destination.name)
                }
            }
        }
        if (isLoadingRoute) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
