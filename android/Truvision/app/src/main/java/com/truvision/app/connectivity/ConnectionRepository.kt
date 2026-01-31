package com.truvision.app.connectivity

import android.content.Context

data class ConnectionState(
    val status: String = "Not connected",
    val httpCode: Int? = null,
    val baseUrl: String? = null,
    val latencyMs: Long? = null
)

class ConnectionRepository(context: Context) {
    private val discovery = UsbIpDiscovery()
    private val usbPrefs = UsbPreferences(context)
    private val overridePrefs = OverridePreferences(context)

    suspend fun checkConnection(): ConnectionState {
        val isOverride = overridePrefs.isOverrideEnabled()
        
        return if (isOverride) {
            checkOverrideConnection()
        } else {
            checkUsbDiscoveryConnection()
        }
    }

    private suspend fun checkOverrideConnection(): ConnectionState {
        val overrideUrl = overridePrefs.getOverrideUrl()
        val startTime = System.currentTimeMillis()
        
        return try {
            val url = "${overrideUrl}/health"
            val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            conn.apply {
                requestMethod = "GET"
                instanceFollowRedirects = false
                connectTimeout = 2000
                readTimeout = 2000
            }
            conn.connect()
            val code = conn.responseCode
            conn.disconnect()
            
            val latency = System.currentTimeMillis() - startTime
            
            ConnectionState(
                status = if (code in 200..299) "Connected" else "Not connected",
                httpCode = code,
                baseUrl = overrideUrl,
                latencyMs = latency
            )
        } catch (e: Exception) {
            ConnectionState(
                status = "Not connected",
                httpCode = null,
                baseUrl = overrideUrl,
                latencyMs = System.currentTimeMillis() - startTime
            )
        }
    }

    private suspend fun checkUsbDiscoveryConnection(): ConnectionState {
        val lastKnownIp = usbPrefs.getLastSuccessfulIp()
        val result = discovery.discoverUsbBaseUrl(lastKnownIp)
        
        return if (result.baseUrl != null && result.httpCode != null && result.httpCode in 200..299) {
            val ip = result.baseUrl.replace("http://", "").replace(":8000", "")
            usbPrefs.saveLastSuccessfulIp(ip)
            
            ConnectionState(
                status = "Connected",
                httpCode = result.httpCode,
                baseUrl = result.baseUrl,
                latencyMs = result.latencyMs
            )
        } else {
            ConnectionState(
                status = "Not connected",
                httpCode = result.httpCode,
                baseUrl = result.baseUrl,
                latencyMs = result.latencyMs
            )
        }
    }
}
