package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.SessionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NovaCosmicBg
import com.example.ui.viewmodel.NovaViewModel

enum class NovaScreen {
    CHAT,
    SESSIONS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NovaCosmicBg
                ) {
                    NovaApp()
                }
            }
        }
    }
}

@Composable
fun NovaApp(viewModel: NovaViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(NovaScreen.CHAT) }

    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
            NovaScreen.CHAT -> {
                ChatScreen(
                    viewModel = viewModel,
                    onNavigateToSessions = { currentScreen = NovaScreen.SESSIONS },
                    onNavigateToSettings = { currentScreen = NovaScreen.SETTINGS }
                )
            }
            NovaScreen.SESSIONS -> {
                BackHandler { currentScreen = NovaScreen.CHAT }
                SessionsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = NovaScreen.CHAT },
                    onSessionSelected = { currentScreen = NovaScreen.CHAT }
                )
            }
            NovaScreen.SETTINGS -> {
                BackHandler { currentScreen = NovaScreen.CHAT }
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = NovaScreen.CHAT }
                )
            }
        }
    }
}
