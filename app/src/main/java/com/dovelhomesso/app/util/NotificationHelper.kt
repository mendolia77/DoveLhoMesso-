package com.dovelhomesso.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dovelhomesso.app.MainActivity
import com.dovelhomesso.app.R

object NotificationHelper {
    private const val CHANNEL_ID = "expiry_channel"
    private const val CHANNEL_NAME = "Scadenze Documenti"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifiche per documenti in scadenza"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showExpiryNotification(context: Context, docId: Long, title: String, daysLeft: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val message = when {
            daysLeft <= 0 -> "Il documento '$title' scade OGGI o è scaduto!"
            else -> "Il documento '$title' scade tra $daysLeft giorni."
        }

        // Use a generic icon if app icon not available, but usually mipmap/ic_launcher exists.
        // Using android.R.drawable.ic_dialog_info as safe fallback if R.drawable.ic_launcher_foreground fails compilation (it shouldn't)
        // But better to use R.mipmap.ic_launcher if available or R.drawable.ic_launcher_foreground
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle("Documento in scadenza")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(docId.toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
