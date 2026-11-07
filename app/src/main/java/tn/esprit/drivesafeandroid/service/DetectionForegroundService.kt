package tn.esprit.drivesafeandroid.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import tn.esprit.drivesafeandroid.R
import tn.esprit.drivesafeandroid.data.EventRepository
import tn.esprit.drivesafeandroid.logic.DecisionTree
import tn.esprit.drivesafeandroid.logic.FeatureVector
import tn.esprit.drivesafeandroid.logic.Hysteresis
import tn.esprit.drivesafeandroid.logic.HysteresisState
import tn.esprit.drivesafeandroid.logic.LwmsConfig
import tn.esprit.drivesafeandroid.logic.LwmsMonitor
import tn.esprit.drivesafeandroid.logic.Severity

class DetectionForegroundService : Service() {
    private var tts: TextToSpeech? = null

    companion object {
        const val CHANNEL_ID = "LWMS_Detection_Service_Channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // TTS ready
            }
        }

        LwmsMonitor.alertCallback = { severity, firedRule, speed ->
            deliverAlert(severity, firedRule, speed)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createForegroundNotification("LWMS Driver Safety Monitoring Active")
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    fun deliverAlert(severity: Severity, firedRule: String, speed: Double) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val (title, message) = when (severity) {
            Severity.LOW -> "✅ Low Distraction" to "Rule $firedRule matched at ${speed.toInt()} km/h. Advisory warning."
            Severity.MODERATE -> "⚠️Moderate Distraction" to "Rule $firedRule matched at ${speed.toInt()} km/h. Moderate risk."
            Severity.HIGH -> "🚨HIGH DISTRACTION ALERT!" to "CRITICAL RISK! Rule $firedRule at ${speed.toInt()} km/h!"
            Severity.NONE -> return
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)

        when (severity) {
            Severity.LOW -> {
                // Low = Muted notification
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW)
                notificationBuilder.setSilent(true)
            }
            Severity.MODERATE -> {
                // Mid = Simple bip & short vibration
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
                notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            }
            Severity.HIGH -> {
                // High = High-level alert notification (heads-up)
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX)
                notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
            }
            Severity.NONE -> {}
        }

        // Use unique ID so multiple alerts pop up distinctly
        manager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (severity) {
                Severity.LOW -> {
                    // Muted: no haptics
                }
                Severity.MODERATE -> {
                    // Simple bip: 1 short pulse
                    vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                Severity.HIGH -> {
                    // High level: heavy pulse pattern + TTS voice warning
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
                    tts?.speak("High distraction detected! Keep your eyes on the road!", TextToSpeech.QUEUE_FLUSH, null, null)
                }
                Severity.NONE -> {}
            }
        }
    }

    private fun createForegroundNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DriveSafe LWMS Service")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "LWMS Safety Detection Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Runs driver distraction detection and alert delivery"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        LwmsMonitor.alertCallback = null
        tts?.shutdown()
    }
}
