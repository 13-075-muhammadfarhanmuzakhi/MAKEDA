package com.farhanrr.makeda

import android.app.Application
import android.content.Context

class AppContextHolder : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    companion object {
        var appContext: Context? = null
    }
}