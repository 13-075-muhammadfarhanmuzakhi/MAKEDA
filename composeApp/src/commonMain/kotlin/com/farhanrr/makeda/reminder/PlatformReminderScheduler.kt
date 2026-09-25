package com.farhanrr.makeda.reminder

expect object PlatformReminderScheduler {
    fun schedule(id: Int, title: String, message: String, epochMillis: Long)
    fun cancel(id: Int)
}