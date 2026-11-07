package tn.esprit.drivesafeandroid

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import tn.esprit.drivesafeandroid.data.EventRepository
import tn.esprit.drivesafeandroid.service.DetectionForegroundService
import tn.esprit.drivesafeandroid.ui.MainAppScreen
import tn.esprit.drivesafeandroid.ui.SplashScreen

class MainActivity : ComponentActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.MANAGE_OWN_CALLS), 101)

        // Initialize Room persistent database repository
        EventRepository.init(applicationContext)

        // Start our persistent background telemetry foreground service
        startForegroundService(Intent(this, DetectionForegroundService::class.java))

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            if (showSplash) {
                SplashScreen {
                    showSplash = false
                }
            } else {
                MainAppScreen()
            }
        }
    }
}
