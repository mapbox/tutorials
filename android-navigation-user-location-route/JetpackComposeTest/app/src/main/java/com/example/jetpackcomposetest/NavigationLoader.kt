package com.example.jetpackcomposetest

import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.base.route.NavigationRouterCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object NavigationLoader {
    suspend fun loadRoutes(
        mapboxNavigation: MapboxNavigation,
        origin: Point,
        destination: Point
    ): List<NavigationRoute> {
        return suspendCancellableCoroutine { continuation ->

            val routeOptions = RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .coordinatesList(listOf(origin, destination))
                .alternatives(false)
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
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
                    override fun onRoutesReady(
                        routes: List<NavigationRoute>,
                        routerOrigin: String
                    ) {
                        continuation.resume(routes)
                    }

                    override fun onFailure(
                        reasons: List<RouterFailure>,
                        routeOptions: RouteOptions
                    ) {
                        continuation.resumeWithException(
                            RuntimeException("Route request failed: ${reasons.joinToString { it.message.orEmpty() }}")
                        )
                    }

                    override fun onCanceled(
                        routeOptions: RouteOptions,
                        routerOrigin: String
                    ) {

                    }
                }
            )
        }
    }
}
