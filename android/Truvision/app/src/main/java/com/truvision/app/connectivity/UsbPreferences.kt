package com.truvision.app.connectivity

import android.content.Context
import android.content.SharedPreferences

class UsbPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    fun saveLastSuccessfulIp(ip: String) {
        prefs.edit()
            .putString(KEY_LAST_IP, ip)
            .apply()
    }
    
    fun getLastSuccessfulIp(): String? {
        return prefs.getString(KEY_LAST_IP, null)
    }
    
    fun clearLastIp() {
        prefs.edit()
            .remove(KEY_LAST_IP)
            .apply()
    }
    
    companion object {
        private const val PREFS_NAME = "usb_connection_prefs"
        private const val KEY_LAST_IP = "last_successful_ip"
    }
}
