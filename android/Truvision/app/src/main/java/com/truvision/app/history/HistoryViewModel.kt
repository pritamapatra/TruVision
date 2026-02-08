package com.truvision.app.history

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

sealed class HistoryState {
    object Loading : HistoryState()
    data class Success(val samples: List<SampleResponse>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ConnectionRepository(application)
    
    private val _historyState = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val historyState: StateFlow<HistoryState> = _historyState.asStateFlow()
    
    init {
        fetchSamples()
    }
    
    fun fetchSamples() {
        viewModelScope.launch {
            _historyState.value = HistoryState.Loading
            try {
                val baseUrl = repository.getCurrentBaseUrl()
                val api = RetrofitClient.getApi(baseUrl)
                val response = api.getSamples()
                
                if (response.isSuccessful && response.body() != null) {
                    _historyState.value = HistoryState.Success(response.body()!!)
                } else {
                    _historyState.value = HistoryState.Error("Failed to fetch samples: ${response.code()}")
                }
            } catch (e: Exception) {
                _historyState.value = HistoryState.Error("Network error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun deleteSample(jobId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val baseUrl = repository.getCurrentBaseUrl()
                val api = RetrofitClient.getApi(baseUrl)
                val response = api.deleteSample(jobId)
                if (response.isSuccessful) {
                    onResult(true, "Sample deleted")
                    // Refresh list after delete
                    fetchSamples()
                } else {
                    onResult(false, "Failed to delete sample (code ${response.code()})")
                }
            } catch (e: Exception) {
                onResult(false, "Network error: ${e.message ?: "Unknown error"}")
            }
        }
    }

}