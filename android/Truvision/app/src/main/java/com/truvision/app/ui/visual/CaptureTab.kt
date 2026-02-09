package com.truvision.app.ui.visual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.truvision.app.visual.VisualViewModel
import com.truvision.app.visual.CaptureState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CaptureTab(
    viewModel: VisualViewModel = viewModel()
) {
    val captureState by viewModel.captureState.collectAsState()
    var capturedImageId by remember { mutableStateOf<String?>(null) }
    var showSuccessToast by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast && capturedImageId != null) {
            snackbarHostState.showSnackbar("Image captured! ID: $capturedImageId")
            delay(4000)
            showSuccessToast = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_camera),
                            contentDescription = "Camera",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Camera Control",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(
                                color = Color(0xFFE8F4F8),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (captureState) {
                            is CaptureState.Starting -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(60.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 6.dp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Capturing image...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(android.R.drawable.ic_menu_camera),
                                        contentDescription = "Live camera feed",
                                        modifier = Modifier.size(80.dp),
                                        tint = Color(0xFF607D8B)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Live camera feed",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            viewModel.startCapture { imageId, message ->
                                if (imageId != null) {
                                    capturedImageId = imageId
                                    showSuccessToast = true
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = message ?: "Capture failed"
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = captureState is CaptureState.Idle
                    ) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_camera),
                            contentDescription = "Capture",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (captureState) {
                                is CaptureState.Starting -> "Capturing..."
                                else -> "Capture Image"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    if (captureState is CaptureState.Starting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (captureState is CaptureState.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (captureState as CaptureState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
