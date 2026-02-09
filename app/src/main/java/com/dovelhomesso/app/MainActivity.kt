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

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.zIndex

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
                    val navController = rememberNavController()
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavGraph(
                            navController = navController,
                            viewModel = viewModel
                        )
                        
                        if (!isUnlocked) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(1f),
                                color = MaterialTheme.colorScheme.background
                            ) {
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
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if we need to lock the app due to inactivity
        if (PinManager.isPinSet(this)) {
            mainViewModel?.checkLockTimeout()
        }
    }

    override fun onStop() {
        super.onStop()
        // Record the time when app goes to background
        // Only if it's not a configuration change (rotation)
        if (!isChangingConfigurations && PinManager.isPinSet(this)) {
            mainViewModel?.onAppBackgrounded()
        }
    }
}
