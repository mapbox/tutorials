package com.example.nav_turn_tutorial

import android.content.Context
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

data class PreparedNavigation(
    val routes: List<NavigationRoute>
) {
    val distanceMeters: Double
        get() = routes.first().directionsRoute.distance()
}

class NavigationLoader(private val context: Context) {

    suspend fun loadNavigation(origin: Point, destination: Point): PreparedNavigation =
        suspendCancellableCoroutine { continuation ->
            val mapboxNavigation = MapboxNavigationApp.current()
                ?: error("MapboxNavigationApp is not set up")

            val routeOptions = RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(context)
                .coordinatesList(listOf(origin, destination))
                .alternatives(false)
                .build()

            mapboxNavigation.requestRoutes(
                routeOptions,
                object : NavigationRouterCallback {
                    override fun onRoutesReady(
                        routes: List<NavigationRoute>,
                        routerOrigin: RouterOrigin
                    ) {
                        continuation.resume(PreparedNavigation(routes))
                    }

                    override fun onFailure(
                        reasons: List<RouterFailure>,
                        routeOptions: RouteOptions
                    ) {
                        continuation.resumeWithException(
                            IllegalStateException("Route request failed: $reasons")
                        )
                    }

                    override fun onCanceled(
                        routeOptions: RouteOptions,
                        routerOrigin: RouterOrigin
                    ) {
                        continuation.cancel()
                    }
                }
            )
        }
}
