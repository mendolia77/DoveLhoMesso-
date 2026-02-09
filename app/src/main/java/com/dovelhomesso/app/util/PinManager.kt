package com.dovelhomesso.app.util

import android.content.Context
import androidx.core.content.edit

object PinManager {
    private const val PREFS_NAME = "app_security"
    private const val KEY_PIN = "user_pin"

    fun isPinSet(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_PIN)
    }

    fun checkPin(context: Context, pin: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPin = prefs.getString(KEY_PIN, "")
        return savedPin == pin
    }

    fun setPin(context: Context, pin: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_PIN, pin) }
    }

    fun removePin(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_PIN) }
    }
}
