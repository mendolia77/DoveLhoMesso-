package com.dovelhomesso.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.dovelhomesso.app.ui.navigation.NavGraph
import com.dovelhomesso.app.ui.theme.DoveLhoMessoTheme
import com.dovelhomesso.app.ui.viewmodels.MainViewModel

import androidx.compose.runtime.*
import com.dovelhomesso.app.ui.screens.PinScreen
import com.dovelhomesso.app.util.PinManager

class MainActivity : ComponentActivity() {
    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            DoveLhoMessoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val viewModel: MainViewModel = viewModel()
                    mainViewModel = viewModel
                    
                    // Initialize unlock state based on PIN existence if not already initialized
                    LaunchedEffect(Unit) {
                        if (!PinManager.isPinSet(context)) {
                            viewModel.unlockApp()
                        }
                    }

                    val isUnlocked by viewModel.isAppUnlocked.collectAsState()
                    
                    if (isUnlocked) {
                        val navController = rememberNavController()
                        
                        NavGraph(
                            navController = navController,
                            viewModel = viewModel
                        )
                    } else {
                        PinScreen(
                            title = "Inserisci PIN per accedere",
                            onPinCorrect = { viewModel.unlockApp() },
                            isSettingPin = false,
                            validatePin = { input -> PinManager.checkPin(context, input) }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Lock the app if it's not a configuration change (e.g. rotation)
        // and if a PIN is set.
        if (!isChangingConfigurations && PinManager.isPinSet(this)) {
            mainViewModel?.lockApp()
        }
    }
}
