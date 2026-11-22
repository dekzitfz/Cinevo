package com.app.cinevo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.cinevo.presentation.navigation.CinevoNavHost
import com.app.cinevo.presentation.theme.CinevoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Transparent status bar and (gesture) navigation bar; icon colors follow the system dark/light
        // setting, matching CinevoTheme. Screens consume WindowInsets themselves.
        enableEdgeToEdge()
        setContent {
            CinevoTheme {
                CinevoNavHost()
            }
        }
    }
}
