package com.truvision.app.connectivity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class UsbResolveResult(
    val baseUrl: String?,
    val httpCode: Int?
)

class ConnectionResolver(
    private val connectTimeoutMs: Int = 1_000,
    private val readTimeoutMs: Int = 1_000
) {
    suspend fun resolveUsbBaseUrl(): UsbResolveResult {
        val url = ConnectivityConstants.USB_BASE_URL + ConnectivityConstants.HEALTH_PATH
        val code = probe(url)
        return if (code != null && code in 200..299) {
            UsbResolveResult(
                baseUrl = ConnectivityConstants.USB_BASE_URL,
                httpCode = code
            )
        } else {
            UsbResolveResult(
                baseUrl = null,
                httpCode = code
            )
        }
    }

    private suspend fun probe(url: String): Int? = withContext(Dispatchers.IO) {
        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = false
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
            }
            conn.connect()
            val code = conn.responseCode
            conn.disconnect()
            code
        } catch (e: Exception) {
            null
        }
    }
}
