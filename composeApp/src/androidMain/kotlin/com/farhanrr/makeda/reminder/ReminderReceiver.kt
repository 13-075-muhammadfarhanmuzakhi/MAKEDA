package com.farhanrr.makeda.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.farhanrr.makeda.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Pengingat MAKEDA"
        val message = intent.getStringExtra("message") ?: ""
        val id = intent.getIntExtra("id", 0)
        val repeat = intent.getBooleanExtra("repeat", false)
        val hour = intent.getIntExtra("hour", 20)
        val minute = intent.getIntExtra("minute", 0)

        // Pengingat harian: jadwalkan lagi untuk besok sebelum menampilkan notifikasi
        if (repeat) {
            PlatformReminderScheduler.scheduleDailyWith(context, id, title, message, hour, minute)
        }

        val channelId = "makeda_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Keuangan",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.ic_stat_makeda)
            .setColor(0xFF3D5AFE.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            manager.notify(id, notification)
        } catch (_: SecurityException) {
            // izin notifikasi belum diberikan
        }
    }
}
