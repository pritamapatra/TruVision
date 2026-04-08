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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

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
    private val _previewUrl = MutableStateFlow<String?>(null)
    val previewUrl: StateFlow<String?> = _previewUrl.asStateFlow()
    private val _previewTick = MutableStateFlow(0L)
    val previewTick: StateFlow<Long> = _previewTick.asStateFlow()
    private val _previewBitmap = MutableStateFlow<ImageBitmap?>(null)
    val previewBitmap: StateFlow<ImageBitmap?> = _previewBitmap.asStateFlow()
    private val _samples = MutableStateFlow<List<com.truvision.app.api.SampleResponse>>(emptyList())
    val samples: StateFlow<List<com.truvision.app.api.SampleResponse>> = _samples.asStateFlow()
    private val _samplesLoading = MutableStateFlow(false)
    val samplesLoading: StateFlow<Boolean> = _samplesLoading.asStateFlow()
    private var isPreviewRunning = false
    private val _baseUrl = MutableStateFlow<String?>( null)
    val baseUrl: StateFlow<String?> = _baseUrl.asStateFlow()

    
    fun setPreviewUrl(baseUrl: String) {
        _previewUrl.value = "$baseUrl/preview"
    }


    fun startPreview() {
        if (isPreviewRunning) return
        isPreviewRunning = true
        viewModelScope.launch {
            val baseUrl = repository.getCurrentBaseUrl()
            _previewUrl.value = "$baseUrl/preview"
            while (isActive && isPreviewRunning) {
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        val conn = URL("$baseUrl/preview?t=${System.currentTimeMillis()}").openConnection() as HttpURLConnection
                        conn.connectTimeout = 2000
                        conn.readTimeout = 2000
                        val data = conn.inputStream.readBytes()
                        conn.disconnect()
                        data
                    }
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) _previewBitmap.value = bmp.asImageBitmap()
                } catch (e: Exception) {
                    // keep showing last frame on error
                }
                delay(500)
            }
        }
    }

    fun stopPreview() {
        isPreviewRunning = false
    }

    fun startCapture(onCaptureComplete: (String?, String?) -> Unit) {
        viewModelScope.launch {
            _captureState.value = CaptureState.Starting
            
            try {
                val baseUrl = repository.getCurrentBaseUrl()
                val api = RetrofitClient.getApi(baseUrl)
                
                val response = api.startCapture()
                
                if (response.isSuccessful && response.body() != null) {
                    val imageId = response.body()!!.jobId
                    val message = "Image captured"
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
    
    fun loadSamples() {
        viewModelScope.launch {
            _samplesLoading.value = true
            try {
                val baseUrl = repository.getCurrentBaseUrl()
                _baseUrl.value = baseUrl
                val api = RetrofitClient.getApi(baseUrl)
                val response = api.getSamples()
                if (response.isSuccessful) {
                    _samples.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // keep existing list on error
            } finally {
                _samplesLoading.value = false
            }
        }
    }

    fun deleteSamples(jobIds: Set<String>) {
        viewModelScope.launch {
            try {
                val baseUrl = repository.getCurrentBaseUrl()
                val api = RetrofitClient.getApi(baseUrl)
                jobIds.forEach { jobId ->
                    api.deleteSample(jobId)
                }
                loadSamples()
            } catch (e: Exception) {
                // keep existing list on error
            }
        }
    }

    fun resetState() {
        _captureState.value = CaptureState.Idle
    }
}
