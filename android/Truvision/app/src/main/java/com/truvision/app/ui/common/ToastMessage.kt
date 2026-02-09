package com.truvision.app.ui.common

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

data class ToastMessage(
    val message: String,
    val type: ToastType = ToastType.INFO,
    val duration: Long = 3000L
)

enum class ToastType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}

class ToastState {
    private val _currentToast = mutableStateOf<ToastMessage?>(null)
    val currentToast: State<ToastMessage?> = _currentToast
    
    fun show(message: String, type: ToastType = ToastType.INFO, duration: Long = 3000L) {
        _currentToast.value = ToastMessage(message, type, duration)
    }
    
    fun dismiss() {
        _currentToast.value = null
    }
}

@Composable
fun ToastHost(toastState: ToastState) {
    val toast = toastState.currentToast.value
    
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(toast.duration)
            toastState.dismiss()
        }
    }
    
    if (toast != null) {
        val backgroundColor = when (toast.type) {
            ToastType.SUCCESS -> Color(0xFF4CAF50)
            ToastType.ERROR -> Color(0xFFF44336)
            ToastType.WARNING -> Color(0xFFFFA726)
            ToastType.INFO -> Color(0xFF2196F3)
        }
        
        Snackbar(
            snackbarData = object : SnackbarData {
                override val visuals: SnackbarVisuals = object : SnackbarVisuals {
                    override val message: String = toast.message
                    override val actionLabel: String? = null
                    override val withDismissAction: Boolean = true
                    override val duration: SnackbarDuration = SnackbarDuration.Short
                }
                override fun dismiss() {
                    toastState.dismiss()
                }
                override fun performAction() {}
            },
            containerColor = backgroundColor,
            contentColor = Color.White
        )
    }
}
