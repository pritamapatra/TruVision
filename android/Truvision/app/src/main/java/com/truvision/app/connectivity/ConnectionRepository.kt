package com.truvision.app.connectivity

import android.app.Application
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class ConnectionResult(
    val isSuccess: Boolean,
    val httpCode: Int? = null,
    val latencyMs: Long? = null,
    val errorMessage: String? = null
)

class ConnectionRepository(private val application: Application) {
    private val overridePrefs = OverridePreferences(application)
    private val usbPrefs = UsbPreferences(application)

    fun getCurrentBaseUrl(): String {
        val url = if (overridePrefs.isOverrideEnabled()) {
            overridePrefs.getOverrideUrl()
        } else {
            val lastIp = usbPrefs.getLastSuccessfulIp()
            if (lastIp != null) {
                "http://$lastIp:8000"
            } else {
                ConnectivityConstants.USB_BASE_URL
            }
        }
        Log.d("ConnectionRepository", "getCurrentBaseUrl() returning: $url")
        Log.d("ConnectionRepository", "Override enabled: ${overridePrefs.isOverrideEnabled()}")
        return url
    }

    suspend fun checkConnection(): ConnectionResult = withContext(Dispatchers.IO) {
        val baseUrl = getCurrentBaseUrl()
        val healthUrl = "$baseUrl/${ConnectivityConstants.HEALTH_PATH}"
        
        Log.d("ConnectionRepository", "checkConnection() starting")
        Log.d("ConnectionRepository", "Full health URL: $healthUrl")
        
        val startTime = System.currentTimeMillis()
        
        try {
            val url = URL(healthUrl)
            Log.d("ConnectionRepository", "Opening connection to: $url")
            
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                instanceFollowRedirects = false
            }
            
            Log.d("ConnectionRepository", "Connecting...")
            conn.connect()
            
            val code = conn.responseCode
            val latency = System.currentTimeMillis() - startTime
            
            Log.d("ConnectionRepository", "Response code: $code, latency: ${latency}ms")
            
            conn.disconnect()
            
            ConnectionResult(
                isSuccess = code == 200,
                httpCode = code,
                latencyMs = latency
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            Log.e("ConnectionRepository", "Connection failed after ${latency}ms", e)
            Log.e("ConnectionRepository", "Error type: ${e.javaClass.simpleName}")
            Log.e("ConnectionRepository", "Error message: ${e.message}")
            
            ConnectionResult(
                isSuccess = false,
                latencyMs = latency,
                errorMessage = e.message
            )
        }
    }

    suspend fun disconnect(): ConnectionResult = withContext(Dispatchers.IO) {
        val baseUrl = getCurrentBaseUrl()
        val disconnectUrl = "$baseUrl/disconnect"
        
        Log.d("ConnectionRepository", "disconnect() starting")
        Log.d("ConnectionRepository", "Disconnect URL: $disconnectUrl")
        
        try {
            val url = URL(disconnectUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 3000
                readTimeout = 3000
            }
            
            conn.connect()
            val code = conn.responseCode
            Log.d("ConnectionRepository", "Disconnect response: $code")
            conn.disconnect()
            
            ConnectionResult(isSuccess = code in 200..299, httpCode = code)
        } catch (e: Exception) {
            Log.e("ConnectionRepository", "Disconnect failed", e)
            ConnectionResult(isSuccess = false, errorMessage = e.message)
        }
    }
}
