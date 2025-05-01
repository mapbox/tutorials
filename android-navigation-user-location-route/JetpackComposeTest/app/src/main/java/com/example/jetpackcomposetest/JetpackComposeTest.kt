package com.example.jetpackcomposetest;

import android.app.Application;
import com.mapbox.dash.sdk.Dash


class JetpackComposeTest : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize the framework
        Dash.init(
            context = this,
            accessToken = getString(R.string.mapbox_access_token)
        )
    }
}