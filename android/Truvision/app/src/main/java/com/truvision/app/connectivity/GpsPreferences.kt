package com.truvision.app.connectivity

import android.content.Context
import android.content.SharedPreferences

class GpsPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun isGpsTaggingEnabled(): Boolean {
        return prefs.getBoolean(KEY_GPS_TAGGING_ENABLED, false)
    }

    fun setGpsTaggingEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_GPS_TAGGING_ENABLED, enabled)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "gps_prefs"
        private const val KEY_GPS_TAGGING_ENABLED = "gps_tagging_enabled"
    }
}
