package com.truvision.app.ui.visual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AnalyzeTab(
    preloadedImage: String?,
    onDetectClick: () -> Unit
) {
    var showDetection by remember { mutableStateOf(true) }
    var isDetecting by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (preloadedImage != null) {
            isDetecting = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
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
                
                Button(
                    onClick = { 
                        isDetecting = true
                        onDetectClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isDetecting
                ) {
                    if (isDetecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Detecting...",
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
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
                
                if (preloadedImage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Image preloaded: $preloadedImage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
                        text = "Analysis Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showDetection = !showDetection }) {
                            Icon(
                                imageVector = if (showDetection) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showDetection) "Hide Detection" else "Show Detection",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Magnify",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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
                    if (isDetecting) {
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
                                text = "Running detection...",
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
                    } else if (showDetection) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(android.R.drawable.ic_menu_view),
                                contentDescription = "Detection visualization",
                                modifier = Modifier.size(80.dp),
                                tint = Color(0xFF607D8B)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Detection overlay will appear here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Detection Legend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (isDetecting) {
                    repeat(5) { index ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(16.dp)
                                    .background(
                                        color = Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(16.dp)
                                    .background(
                                        color = Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                } else {
                    val detectionItems = listOf(
                        Triple("PET", "150μm", "98%"),
                        Triple("PE", "120μm", "94%"),
                        Triple("PS", "110μm", "91%"),
                        Triple("PVC", "135μm", "89%"),
                        Triple("Nylon", "145μm", "96%")
                    )
                    
                    detectionItems.forEach { (type, size, confidence) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$size • $confidence",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        if (type != "Nylon") {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
