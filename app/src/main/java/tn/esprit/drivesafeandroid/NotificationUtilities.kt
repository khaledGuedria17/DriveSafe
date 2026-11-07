package tn.esprit.drivesafeandroid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import tn.esprit.drivesafeandroid.logic.Severity

object NotificationUtilities {
    const val OBD_SPEED = 80

    fun showNotification(context: Context, title: String, message: String, severity: Severity = Severity.MODERATE) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "DriveSafe_CHANNEL",
            "Driver Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for driver safety notifications"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)

        val priority = when (severity) {
            Severity.LOW -> NotificationCompat.PRIORITY_LOW
            Severity.MODERATE -> NotificationCompat.PRIORITY_HIGH
            Severity.HIGH -> NotificationCompat.PRIORITY_MAX
            Severity.NONE -> NotificationCompat.PRIORITY_DEFAULT
        }

        val builder = NotificationCompat.Builder(context, channel.id)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(priority)
            .setAutoCancel(true)

        when (severity) {
            Severity.LOW -> builder.setSilent(true)
            Severity.MODERATE -> builder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            Severity.HIGH -> builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            Severity.NONE -> {}
        }

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
