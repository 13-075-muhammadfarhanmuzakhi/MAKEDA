package com.farhanrr.makeda.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.farhanrr.makeda.util.DAILY_REMINDER_HOUR
import com.farhanrr.makeda.util.DAILY_REMINDER_ID
import com.farhanrr.makeda.util.DAILY_REMINDER_MESSAGE
import com.farhanrr.makeda.util.DAILY_REMINDER_MINUTE
import com.farhanrr.makeda.util.DAILY_REMINDER_TITLE

/** Alarm hilang saat HP restart; ini memasangnya lagi. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val prefs = context.getSharedPreferences("makeda_prefs", Context.MODE_PRIVATE)
        if (prefs.getString("makeda_daily_enabled", "0") == "1") {
            PlatformReminderScheduler.scheduleDailyWith(
                context, DAILY_REMINDER_ID, DAILY_REMINDER_TITLE, DAILY_REMINDER_MESSAGE,
                DAILY_REMINDER_HOUR, DAILY_REMINDER_MINUTE
            )
        }
    }
}
