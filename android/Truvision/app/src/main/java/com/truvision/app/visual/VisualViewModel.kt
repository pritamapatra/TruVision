package com.truvision.app.visual

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.truvision.app.api.RetrofitClient
import com.truvision.app.connectivity.ConnectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CaptureState {
    object Idle : CaptureState()
    object Starting : CaptureState()
    data class Success(val jobId: String) : CaptureState()
    data class Error(val message: String) : CaptureState()
}

class VisualViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ConnectionRepository(application)
    
    private val _captureState = MutableStateFlow<CaptureState>(CaptureState.Idle)
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()
    
    fun startCapture() {
        viewModelScope.launch {
            _captureState.value = CaptureState.Starting
            
            try {
                val baseUrl = repository.getCurrentBaseUrl()
                val api = RetrofitClient.getApi(baseUrl)
                
                val response = api.startCapture()
                
                if (response.isSuccessful && response.body() != null) {
                    val jobId = response.body()!!.jobId
                    _captureState.value = CaptureState.Success(jobId)
                } else {
                    _captureState.value = CaptureState.Error(
                        "Failed to start capture: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _captureState.value = CaptureState.Error(
                    "Network error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun resetState() {
        _captureState.value = CaptureState.Idle
    }
}
