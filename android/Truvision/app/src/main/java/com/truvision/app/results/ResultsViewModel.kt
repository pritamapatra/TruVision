package com.truvision.app.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.truvision.app.api.RetrofitClient
import com.truvision.app.connectivity.ConnectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JobState(
    val jobId: String? = null,
    val status: String = "unknown",
    val detectedCount: Int? = null,
    val isPolling: Boolean = false,
    val error: String? = null
)

class ResultsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ConnectionRepository(application)
    
    private val _jobState = MutableStateFlow(JobState())
    val jobState: StateFlow<JobState> = _jobState.asStateFlow()
    
    private var pollingJob: Job? = null
    
    fun startPolling(jobId: String) {
        pollingJob?.cancel()
        
        _jobState.value = JobState(jobId = jobId, isPolling = true)
        
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val baseUrl = repository.getCurrentBaseUrl()
                    val api = RetrofitClient.getApi(baseUrl)
                    
                    val response = api.getJobStatus(jobId)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        _jobState.value = JobState(
                            jobId = jobId,
                            status = body.status,
                            detectedCount = body.detectedCount,
                            isPolling = body.status != "completed",
                            error = null
                        )
                        
                        if (body.status == "completed") {
                            break
                        }
                    } else {
                        _jobState.value = _jobState.value.copy(
                            error = "Failed to get job status: ${response.code()}"
                        )
                    }
                } catch (e: Exception) {
                    _jobState.value = _jobState.value.copy(
                        error = "Network error: ${e.message}",
                        isPolling = false
                    )
                    break
                }
                
                delay(2500)
            }
        }
    }
    
    fun retryPolling() {
        val currentJobId = _jobState.value.jobId
        if (currentJobId != null) {
            startPolling(currentJobId)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
