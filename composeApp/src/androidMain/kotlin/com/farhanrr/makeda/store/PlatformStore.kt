package com.farhanrr.makeda.store

import android.content.Context
import com.farhanrr.makeda.AppContextHolder

actual object PlatformStore {
    private const val PREFS_NAME = "makeda_prefs"

    private fun prefs() = AppContextHolder.appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    actual fun getString(key: String): String? {
        return prefs()?.getString(key, null)
    }

    actual fun putString(key: String, value: String) {
        prefs()?.edit()?.putString(key, value)?.apply()
    }
}