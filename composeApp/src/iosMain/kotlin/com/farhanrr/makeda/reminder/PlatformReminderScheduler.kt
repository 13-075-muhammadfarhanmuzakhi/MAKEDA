package com.farhanrr.makeda.reminder

actual object PlatformReminderScheduler {
    actual fun schedule(id: Int, title: String, message: String, epochMillis: Long) {
        // TODO: implementasi UNUserNotificationCenter untuk iOS
    }

    actual fun scheduleDaily(id: Int, title: String, message: String, hour: Int, minute: Int) {
        // TODO: implementasi notifikasi harian iOS
    }

    actual fun cancel(id: Int) {
        // TODO: implementasi pembatalan notifikasi iOS
    }
}
