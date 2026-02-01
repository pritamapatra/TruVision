package com.truvision.app.connectivity

import android.content.Context
import android.content.SharedPreferences

class OverridePreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    fun isOverrideEnabled(): Boolean {
        return prefs.getBoolean(KEY_OVERRIDE_ENABLED, false)
    }
    
    fun setOverrideEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_OVERRIDE_ENABLED, enabled)
            .apply()
    }
    
    fun getOverrideUrl(): String {
        return prefs.getString(KEY_OVERRIDE_URL, DEFAULT_URL) ?: DEFAULT_URL
    }
    
    fun setOverrideUrl(url: String) {
        prefs.edit()
            .putString(KEY_OVERRIDE_URL, url)
            .apply()
    }
    
    companion object {
        private const val PREFS_NAME = "override_prefs"
        private const val KEY_OVERRIDE_ENABLED = "override_enabled"
        private const val KEY_OVERRIDE_URL = "override_url"
        private const val DEFAULT_URL = "http://192.168.238.191:8000"
    }
}
