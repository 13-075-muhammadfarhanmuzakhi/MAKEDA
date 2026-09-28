package com.farhanrr.makeda.reminder

actual object PlatformReminderScheduler {
    actual fun schedule(id: Int, title: String, message: String, epochMillis: Long) {
        // TODO: implementasi UNUserNotificationCenter untuk iOS
    }

    actual fun cancel(id: Int) {
        // TODO: implementasi pembatalan notifikasi iOS
    }
}