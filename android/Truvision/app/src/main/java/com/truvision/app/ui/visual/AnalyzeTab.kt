package com.truvision.app.ui.visual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AnalyzeTab(
    preloadedImage: String?,
    viewModel: AnalyzeViewModel = viewModel()
) {
    val analyzeState by viewModel.analyzeState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Analysis Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_search),
                        contentDescription = "Analysis",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analysis Control",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Detect Particles Button
                Button(
                    onClick = { 
                        if (preloadedImage != null) {
                            viewModel.analyzeImage(preloadedImage)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = preloadedImage != null && analyzeState !is AnalyzeState.Analyzing
                ) {
                    when (analyzeState) {
                        is AnalyzeState.Analyzing -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analyzing...",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        is AnalyzeState.Success -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analysis Complete",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Detect",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Detect Particles",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                
                // Show preloaded image info
                if (preloadedImage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Image: $preloadedImage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select an image from Browse tab first",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Detection Results Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detection Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Show particle count if available
                    if (analyzeState is AnalyzeState.Success) {
                        val successState = analyzeState as AnalyzeState.Success
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "${successState.detectedCount} particles",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Results Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = analyzeState) {
                        is AnalyzeState.Idle -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_view),
                                    contentDescription = "Ready",
                                    modifier = Modifier.size(60.dp),
                                    tint = Color(0xFF607D8B)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Select an image and tap Detect Particles",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        is AnalyzeState.Analyzing -> {
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
                                    text = "Running YOLO detection...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier.width(200.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        is AnalyzeState.Success -> {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Detection Summary
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Job ID",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = state.jobId,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Timestamp",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = state.timestamp,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Detection List
                                Text(
                                    text = "Detected Polymers",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                if (state.detections.isEmpty()) {
                                    Text(
                                        text = "No microplastics detected",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                } else {
                                    state.detections.forEachIndexed { index, detection ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row {
                                                Text(
                                                    text = "${index + 1}.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray,
                                                    modifier = Modifier.width(30.dp)
                                                )
                                                Text(
                                                    text = detection.polymerType,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = "${(detection.confidence * 100).toInt()}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        if (index < state.detections.size - 1) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                        
                        is AnalyzeState.Error -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_dialog_alert),
                                    contentDescription = "Error",
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.resetState() }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
