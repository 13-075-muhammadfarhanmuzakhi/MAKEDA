package com.farhanrr.makeda.reminder

expect object PlatformReminderScheduler {
    fun schedule(id: Int, title: String, message: String, epochMillis: Long)
    fun scheduleDaily(id: Int, title: String, message: String, hour: Int, minute: Int)
    fun cancel(id: Int)
}
