package com.farhanrr.makeda.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.farhanrr.makeda.AppContextHolder
import java.util.Calendar

actual object PlatformReminderScheduler {

    actual fun schedule(id: Int, title: String, message: String, epochMillis: Long) {
        val context = AppContextHolder.appContext ?: return
        setAlarm(context, id, title, message, epochMillis, false, 0, 0)
    }

    actual fun scheduleDaily(id: Int, title: String, message: String, hour: Int, minute: Int) {
        val context = AppContextHolder.appContext ?: return
        scheduleDailyWith(context, id, title, message, hour, minute)
    }

    /** Dipakai juga oleh ReminderReceiver & BootReceiver untuk menjadwalkan ulang besok. */
    fun scheduleDailyWith(context: Context, id: Int, title: String, message: String, hour: Int, minute: Int) {
        setAlarm(context, id, title, message, nextTrigger(hour, minute), true, hour, minute)
    }

    actual fun cancel(id: Int) {
        val context = AppContextHolder.appContext ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPending(context, id, "", "", false, 0, 0)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun nextTrigger(hour: Int, minute: Int): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hour)
        c.set(Calendar.MINUTE, minute)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        if (c.timeInMillis - System.currentTimeMillis() < 60_000L) {
            c.add(Calendar.DAY_OF_YEAR, 1)
        }
        return c.timeInMillis
    }

    private fun buildPending(
        context: Context, id: Int, title: String, message: String,
        repeat: Boolean, hour: Int, minute: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("message", message)
            putExtra("repeat", repeat)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }
        return PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setAlarm(
        context: Context, id: Int, title: String, message: String,
        epochMillis: Long, repeat: Boolean, hour: Int, minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPending(context, id, title, message, repeat, hour, minute)
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
        }
    }
}
