package com.example.nav_turn_tutorial

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.dropin.NavigationView
import com.mapbox.navigation.dropin.navigationview.NavigationViewListener

@Composable
fun NavigationScreen(
    routes: List<NavigationRoute>,
    simulateRoute: Boolean,
    onExit: () -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val accessTokenResId = context.resources.getIdentifier(
                "mapbox_access_token",
                "string",
                context.packageName
            )
            val accessToken = context.getString(accessTokenResId)
            var hasStartedActiveNavigation = false
            NavigationView(context, null, accessToken).apply {
                api.routeReplayEnabled(simulateRoute)
                addListener(object : NavigationViewListener() {
                    override fun onActiveNavigation() {
                        hasStartedActiveNavigation = true
                    }

                    override fun onFreeDrive() {
                        if (hasStartedActiveNavigation) {
                            onExit()
                        }
                    }
                })
                api.startActiveGuidance(routes)
            }
        },
        update = { navigationView ->
            navigationView.api.routeReplayEnabled(simulateRoute)
        }
    )
}
