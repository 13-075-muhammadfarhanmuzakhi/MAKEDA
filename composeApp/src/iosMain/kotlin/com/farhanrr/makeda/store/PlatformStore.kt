package com.farhanrr.makeda.store

import platform.Foundation.NSUserDefaults

actual object PlatformStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, key)
    }
}