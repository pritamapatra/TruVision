package com.truvision.app.ui.visual

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.truvision.app.api.AnalyzeImageRequest
import com.truvision.app.api.Detection
import com.truvision.app.api.RetrofitClient
import com.truvision.app.connectivity.ConnectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AnalyzeState {
    object Idle : AnalyzeState()
    object Analyzing : AnalyzeState()
    data class Success(
        val jobId: String,
        val detectedCount: Int,
        val detections: List<Detection>,
        val timestamp: String
    ) : AnalyzeState()
    data class Error(val message: String) : AnalyzeState()
}

class AnalyzeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ConnectionRepository(application)
    
    private val _analyzeState = MutableStateFlow<AnalyzeState>(AnalyzeState.Idle)
    val analyzeState: StateFlow<AnalyzeState> = _analyzeState.asStateFlow()
    
    fun analyzeImage(imageId: String) {
        viewModelScope.launch {
            _analyzeState.value = AnalyzeState.Analyzing
            
            try {
                val baseUrl = repository.getCurrentBaseUrl()
                val api = RetrofitClient.getApi(baseUrl)
                
                // Call analyze endpoint
                val analyzeResponse = api.analyzeImage(AnalyzeImageRequest(imageId))
                
                if (analyzeResponse.isSuccessful && analyzeResponse.body() != null) {
                    val jobId = analyzeResponse.body()!!.jobId
                    
                    // Poll for job completion
                    pollJobStatus(api, jobId)
                } else {
                    val errorMsg = when (analyzeResponse.code()) {
                        404 -> "Image not found. Please recapture."
                        503 -> "Analysis service unavailable."
                        500 -> "Raspberry Pi error. Check connection."
                        else -> "Analysis failed (${analyzeResponse.code()})"
                    }
                    _analyzeState.value = AnalyzeState.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message ?: "Pi not responding"}"
                _analyzeState.value = AnalyzeState.Error(errorMsg)
            }
        }
    }
    
    private suspend fun pollJobStatus(api: com.truvision.app.api.TruVisionApi, jobId: String) {
        var attempts = 0
        val maxAttempts = 30 // 30 seconds timeout
        
        while (attempts < maxAttempts) {
            try {
                kotlinx.coroutines.delay(1000) // Wait 1 second between polls
                
                val statusResponse = api.getJobStatus(jobId)
                
                if (statusResponse.isSuccessful && statusResponse.body() != null) {
                    val job = statusResponse.body()!!
                    
                    when (job.status) {
                        "completed" -> {
                            _analyzeState.value = AnalyzeState.Success(
                                jobId = job.jobId,
                                detectedCount = job.detectedCount ?: 0,
                                detections = job.detections ?: emptyList(),
                                timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                            )
                            return
                        }
                        "failed" -> {
                            _analyzeState.value = AnalyzeState.Error("Detection failed. Try recapturing image.")
                            return
                        }
                        "running" -> {
                            // Continue polling
                            attempts++
                        }
                        else -> {
                            attempts++
                        }
                    }
                } else {
                    _analyzeState.value = AnalyzeState.Error("Failed to check analysis status")
                    return
                }
            } catch (e: Exception) {
                _analyzeState.value = AnalyzeState.Error("Polling error: ${e.message}")
                return
            }
        }
        
        _analyzeState.value = AnalyzeState.Error("Analysis timeout. Please try again.")
    }
    
    fun resetState() {
        _analyzeState.value = AnalyzeState.Idle
    }
}
