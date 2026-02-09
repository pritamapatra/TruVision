package com.truvision.app.ui.visual

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.truvision.app.api.RetrofitClient
import com.truvision.app.api.SampleResponse
import com.truvision.app.connectivity.ConnectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SampleItem(
    val jobId: String,
    val timestamp: String,
    val status: String,
    val detectedCount: Int?
)

class BrowseViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ConnectionRepository(application)
    
    private val _samples = MutableStateFlow<List<SampleItem>>(emptyList())
    val samples: StateFlow<List<SampleItem>> = _samples.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadSamples() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val baseUrl = repository.getCurrentBaseUrl()
                val api = RetrofitClient.getApi(baseUrl)
                
                val response = api.getSamples()
                
                if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                    // Real data from backend
                    val samplesData = response.body()!!
                    _samples.value = samplesData.map { sampleResponse ->
                        SampleItem(
                            jobId = sampleResponse.jobId,
                            timestamp = sampleResponse.timestamp ?: "",
                            status = sampleResponse.status,
                            detectedCount = sampleResponse.detectedCount
                        )
                    }
                } else {
                    // Mock data fallback for testing
                    _samples.value = listOf(
                        SampleItem("sample001", "2026-02-09 14:30:22", "Pending", null),
                        SampleItem("sample002", "2026-02-09 14:25:18", "Analyzed", 5),
                        SampleItem("sample003", "2026-02-09 14:20:45", "Analyzed", 8),
                        SampleItem("sample004", "2026-02-09 14:15:32", "Analyzed", 3)
                    )
                    _error.value = "Using mock data (Pi disconnected)"
                }
            } catch (e: Exception) {
                // Mock data fallback on network error
                _samples.value = listOf(
                    SampleItem("sample001", "2026-02-09 14:30:22", "Pending", null),
                    SampleItem("sample002", "2026-02-09 14:25:18", "Analyzed", 5),
                    SampleItem("sample003", "2026-02-09 14:20:45", "Analyzed", 8),
                    SampleItem("sample004", "2026-02-09 14:15:32", "Analyzed", 3)
                )
                _error.value = "Using mock data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    suspend fun deleteImages(jobIds: List<String>): Boolean {
        return try {
            val baseUrl = repository.getCurrentBaseUrl()
            val api = RetrofitClient.getApi(baseUrl)
            
            var allSuccess = true
            for (jobId in jobIds) {
                val response = api.deleteSample(jobId)
                if (!response.isSuccessful) {
                    allSuccess = false
                }
            }
            
            if (allSuccess) {
                loadSamples()
            }
            allSuccess
        } catch (e: Exception) {
            _error.value = "Delete failed: ${e.message}"
            false
        }
    }
    
    fun getBaseUrl(): String {
        return repository.getCurrentBaseUrl()
    }
}
