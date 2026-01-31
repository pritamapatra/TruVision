package com.truvision.app.connectivity

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class DiscoveryResult(
    val baseUrl: String?,
    val httpCode: Int?,
    val latencyMs: Long?
)

class UsbIpDiscovery(
    private val connectTimeoutMs: Int = 2000,
    private val readTimeoutMs: Int = 2000
) {
    
    suspend fun discoverUsbBaseUrl(lastKnownIp: String? = null): DiscoveryResult {
        val candidates = buildCandidateList(lastKnownIp)
        
        Log.d(TAG, "Starting USB discovery with ${candidates.size} candidates")
        
        for (candidate in candidates) {
            val result = probeCandidate(candidate)
            if (result.httpCode in 200..299) {
                Log.d(TAG, "Discovery success: $candidate (${result.httpCode}, ${result.latencyMs}ms)")
                return result
            }
        }
        
        Log.w(TAG, "Discovery failed: no candidate returned 2xx")
        return DiscoveryResult(null, null, null)
    }
    
    private fun buildCandidateList(lastKnownIp: String?): List<String> {
        val candidates = mutableListOf<String>()
        
        if (!lastKnownIp.isNullOrBlank()) {
            candidates.add("http://$lastKnownIp:8000")
        }
        
        candidates.addAll(listOf(
            "http://192.168.231.240:8000",
            "http://192.168.42.42:8000",
            "http://192.168.42.1:8000",
            "http://192.168.231.1:8000"
        ))
        
        return candidates.distinct()
    }
    
    private suspend fun probeCandidate(baseUrl: String): DiscoveryResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val url = "$baseUrl/health"
            Log.d(TAG, "=== Probing $url ===")
            Log.d(TAG, "Thread: ${Thread.currentThread().name}")
            
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.apply {
                requestMethod = "GET"
                instanceFollowRedirects = false
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
            }
            
            conn.connect()
            val code = conn.responseCode
            val latency = System.currentTimeMillis() - startTime
            Log.d(TAG, "SUCCESS: $baseUrl returned $code in ${latency}ms")
            conn.disconnect()
            
            DiscoveryResult(baseUrl, code, latency)
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            Log.e(TAG, "FAILED: $baseUrl after ${latency}ms", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}, message: ${e.message}")
            DiscoveryResult(null, null, latency)
        }
    }
    
    companion object {
        private const val TAG = "UsbIpDiscovery"
    }
}
