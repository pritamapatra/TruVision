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
    
    fun startCapture(onCaptureComplete: (String?, String?) -> Unit) {
        viewModelScope.launch {
            _captureState.value = CaptureState.Starting
            
            try {
                val baseUrl = repository.getCurrentBaseUrl()
                val api = RetrofitClient.getApi(baseUrl)
                
                val response = api.captureImage()
                
                if (response.isSuccessful && response.body() != null) {
                    val imageId = response.body()!!.imageId
                    val message = response.body()!!.message
                    _captureState.value = CaptureState.Idle
                    onCaptureComplete(imageId, message)
                } else {
                    val errorMsg = when (response.code()) {
                        503 -> "Camera unavailable. Check USB microscope connection."
                        500 -> "Raspberry Pi disconnected. Reconnect device."
                        else -> "Capture failed: ${response.code()}"
                    }
                    _captureState.value = CaptureState.Error(errorMsg)
                    onCaptureComplete(null, errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message ?: "Pi not responding"}"
                _captureState.value = CaptureState.Error(errorMsg)
                onCaptureComplete(null, errorMsg)
            }
        }
    }
    
    fun resetState() {
        _captureState.value = CaptureState.Idle
    }
}
