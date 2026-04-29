package com.example.acremotecontrol

import android.content.Context

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveImei(imei: String) {
        prefs.edit().putString(KEY_IMEI, imei).apply()
    }

    fun getImei(): String? {
        return prefs.getString(KEY_IMEI, null)
    }

    companion object {
        private const val PREFS_NAME = "ac_remote_prefs"
        private const val KEY_IMEI = "imei"
    }
}
