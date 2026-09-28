package com.farhanrr.makeda.store

expect object PlatformStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}