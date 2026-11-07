package tn.esprit.drivesafeandroid

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import tn.esprit.drivesafeandroid.logic.LwmsMonitor
import tn.esprit.drivesafeandroid.ml.RiskClassifier
import kotlin.random.Random

class GestureDetectionService : AccessibilityService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("⚠️ Success", "Social media service started successfully")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                LwmsMonitor.reportScroll()
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                LwmsMonitor.reportTap()
            }
        }
    }

    override fun onInterrupt() {}
}
