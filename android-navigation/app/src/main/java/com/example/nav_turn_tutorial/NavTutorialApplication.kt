package com.example.nav_turn_tutorial

import android.app.Application
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

class NavTutorialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!MapboxNavigationApp.isSetup()) {
            val accessTokenResId = resources.getIdentifier(
                "mapbox_access_token",
                "string",
                packageName
            )
            MapboxNavigationApp.setup(
                NavigationOptions.Builder(this)
                    .accessToken(getString(accessTokenResId))
                    .build()
            )
        }
        MapboxNavigationApp.attachAllActivities(this)
    }
}
