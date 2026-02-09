package com.truvision.app.ui.visual

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BrowseTab(
    onAnalyzeClick: (String) -> Unit
) {
    var selectedImages by remember { mutableStateOf(setOf<String>()) }
    
    val sampleImages = remember {
        listOf(
            ImageItem("Sample_001.jpg", "2024-01-15 14:30:22", "Analyzed", 5),
            ImageItem("Sample_002.jpg", "2024-01-15 14:25:18", "Pending", null),
            ImageItem("Sample_003.jpg", "2024-01-15 14:20:45", "Analyzed", 8),
            ImageItem("Sample_004.jpg", "2024-01-15 14:15:32", "Analyzed", 3)
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search images...") },
            leadingIcon = {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_search),
                    contentDescription = "Search"
                )
            },
            singleLine = true
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_view),
                        contentDescription = "Gallery",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Image Gallery (" + sampleImages.size.toString() + ")",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (selectedImages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        if (selectedImages.size == 1) {
                            Button(
                                onClick = {
                                    selectedImages.firstOrNull()?.let { imageId ->
                                        onAnalyzeClick(imageId)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Analyze")
                            }
                        }
                        
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete (" + selectedImages.size.toString() + ")")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sampleImages.size) { index ->
                        ImageCard(
                            image = sampleImages[index],
                            isSelected = selectedImages.contains(sampleImages[index].filename),
                            onSelectionChange = { selected ->
                                selectedImages = if (selected) {
                                    selectedImages + sampleImages[index].filename
                                } else {
                                    selectedImages - sampleImages[index].filename
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageCard(
    image: ImageItem,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable { onSelectionChange(!isSelected) },
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFE8F4F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_camera),
                    contentDescription = "Image placeholder",
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF607D8B)
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Text(
                    text = image.filename,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = image.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Badge(
                        containerColor = if (image.status == "Analyzed") 
                            Color(0xFF1976D2) else Color(0xFF9E9E9E)
                    ) {
                        Text(
                            text = image.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    
                    if (image.particleCount != null) {
                        Text(
                            text = image.particleCount.toString() + " particles",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

data class ImageItem(
    val filename: String,
    val timestamp: String,
    val status: String,
    val particleCount: Int?
)
