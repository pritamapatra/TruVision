package com.truvision.app.results

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.truvision.app.api.Detection
import com.truvision.app.api.RetrofitClient
import com.truvision.app.connectivity.ConnectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


data class JobState(
    val jobId: String? = null,
    val status: String = "unknown",
    val detectedCount: Int? = null,
    val detections: List<Detection>? = null,
    val imagePath: String? = null,
    val isPolling: Boolean = false,
    val error: String? = null,
    val elapsedSeconds: Int = 0
)

class ResultsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ConnectionRepository(application)
    
    private val _jobState = MutableStateFlow(JobState())
    val jobState: StateFlow<JobState> = _jobState.asStateFlow()
    
    private var pollingJob: Job? = null
    private var timerJob: Job? = null
    
    fun startPolling(jobId: String) {
        cancelPolling()
        
        _jobState.value = JobState(jobId = jobId, isPolling = true, status = "running")
        
        startTimer()
        
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val baseUrl = repository.getCurrentBaseUrl()
                    val api = RetrofitClient.getApi(baseUrl)
                    
                    val response = api.getJobStatus(jobId)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val isCompleted = body.status == "completed"
                        
                        _jobState.value = _jobState.value.copy(
                            status = body.status,
                            detectedCount = body.detectedCount,
                            detections = body.detections,
                            imagePath = body.imagePath,
                            isPolling = !isCompleted,
                            error = null
                        )
                        
                        if (isCompleted) {
                            stopTimer()
                            break
                        }
                    } else {
                        _jobState.value = _jobState.value.copy(
                            error = "Failed to get job status: ${response.code()}",
                            isPolling = false
                        )
                        stopTimer()
                        break
                    }
                } catch (e: Exception) {
                    _jobState.value = _jobState.value.copy(
                        error = "Network error: ${e.message}",
                        isPolling = false
                    )
                    stopTimer()
                    break
                }
                
                delay(2500)
            }
        }
    }
    
    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _jobState.value = _jobState.value.copy(
                    elapsedSeconds = _jobState.value.elapsedSeconds + 1
                )
            }
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
    
    fun cancelPolling() {
        pollingJob?.cancel()
        pollingJob = null
        stopTimer()
        _jobState.value = _jobState.value.copy(isPolling = false)
    }
    
    fun retryPolling() {
        val currentJobId = _jobState.value.jobId
        if (currentJobId != null) {
            startPolling(currentJobId)
        }
    }
    

    fun exportJob(jobId: String) {
        if (jobId.isEmpty()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val baseUrl = repository.getCurrentBaseUrl()
                val api = RetrofitClient.getApi(baseUrl)
                val call = api.exportJob(jobId)
                val response = call.execute()
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    )
                    val outputFile = java.io.File(downloadsDir, "$jobId.zip")
                    
                    val inputStream = body.byteStream()
                    val outputStream = outputFile.outputStream()
                    inputStream.copyTo(outputStream)
                    outputStream.close()
                    inputStream.close()
                    
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            getApplication(),
                            "Exported to Downloads/$jobId.zip",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        404 -> "Sample not found"
                        409 -> "Sample not analyzed yet. Only completed samples can be exported."
                        else -> "Export failed (Error ${response.code()})"
                    }
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(
                            getApplication(),
                            errorMsg,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        getApplication(),
                        "Export failed: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelPolling()
    }

}
