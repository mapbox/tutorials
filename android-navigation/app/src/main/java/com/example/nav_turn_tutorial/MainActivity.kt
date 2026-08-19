package com.example.nav_turn_tutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.nav_turn_tutorial.ui.theme.NavturntutorialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavturntutorialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavTutorialApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun NavTutorialApp(modifier: Modifier = Modifier) {
    var preparedNavigation by remember { mutableStateOf<PreparedNavigation?>(null) }

    val navigation = preparedNavigation
    if (navigation == null) {
        DestinationListScreen(
            modifier = modifier,
            onNavigationReady = { preparedNavigation = it }
        )
    } else {
        NavigationScreen(
            routes = navigation.routes,
            simulateRoute = true,
            onExit = { preparedNavigation = null }
        )
    }
}
