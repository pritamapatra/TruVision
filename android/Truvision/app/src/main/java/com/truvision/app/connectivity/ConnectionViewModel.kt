package com.truvision.app.connectivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ConnectionRepository(application)
    
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()
    
    fun checkConnection() {
        viewModelScope.launch {
            _isChecking.value = true
            _connectionState.value = ConnectionState(status = "Checking...")
            
            val result = repository.checkConnection()
            _connectionState.value = result
            
            _isChecking.value = false
        }
    }
}
