package com.truvision.app.connectivity

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConnectionState(
    val status: String = "unknown",
    val baseUrl: String? = null,
    val httpCode: Int? = null,
    val latencyMs: Long? = null
)

class ConnectionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ConnectionRepository(application)
    
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    init {
        Log.d("ConnectionViewModel", "ViewModel initialized")
        _connectionState.value = ConnectionState(status = "Disconnected")
        Log.d("ConnectionViewModel", "Initial state set to Disconnected")
    }

    fun checkConnection() {
        Log.d("ConnectionViewModel", "checkConnection() called")
        viewModelScope.launch {
            _isChecking.value = true
            Log.d("ConnectionViewModel", "Starting connection check...")
            
            val baseUrl = repository.getCurrentBaseUrl()
            Log.d("ConnectionViewModel", "Using baseUrl: $baseUrl")
            
            val result = repository.checkConnection()
            Log.d("ConnectionViewModel", "Connection result: $result")
            
            _connectionState.value = ConnectionState(
                status = if (result.isSuccess) "Connected" else "Not connected",
                baseUrl = baseUrl,
                httpCode = result.httpCode,
                latencyMs = result.latencyMs
            )
            
            Log.d("ConnectionViewModel", "Updated state: ${_connectionState.value}")
            _isChecking.value = false
        }
    }

    fun disconnect() {
        Log.d("ConnectionViewModel", "disconnect() called")
        viewModelScope.launch {
            _isChecking.value = true
            Log.d("ConnectionViewModel", "Starting disconnect...")
            
            repository.disconnect()
            
            _connectionState.value = ConnectionState(
                status = "Disconnected",
                baseUrl = null,
                httpCode = null,
                latencyMs = null
            )
            
            Log.d("ConnectionViewModel", "Updated state to Disconnected")
            _isChecking.value = false
        }
    }
}
