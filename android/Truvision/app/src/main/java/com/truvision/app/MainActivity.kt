package com.truvision.app
import com.truvision.app.BuildConfig

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import com.truvision.app.api.SampleResponse
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.truvision.app.ui.theme.TruVisionTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.truvision.app.connectivity.ConnectionViewModel
import com.truvision.app.connectivity.ConnectionState
import com.truvision.app.visual.VisualViewModel
import com.truvision.app.history.HistoryViewModel
import com.truvision.app.history.HistoryState
import com.truvision.app.visual.CaptureState
import com.truvision.app.results.ResultsViewModel
import com.truvision.app.results.JobState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TruVisionTheme {
                AppNavigation()
            }
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConnectionStatusBanner(
    connectionState: ConnectionState,
    isChecking: Boolean,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    isCollapsed: Boolean
) {
    if (!isCollapsed) {
        val isConnected = connectionState.status == "Connected"
        val isDisconnected = connectionState.status == "Disconnected" || connectionState.status == "Not connected"
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            color = when {
                isConnected -> Color(0xFFE8F5E9)
                isChecking -> Color(0xFFFFF9C4)
                else -> MaterialTheme.colorScheme.errorContainer
            },
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = when {
                                    isConnected -> Color(0xFF4CAF50)
                                    isChecking -> Color(0xFFFFC107)
                                    else -> Color(0xFFF44336)
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        when {
                            isChecking -> "Checking connection..."
                            isConnected -> "Device Connected"
                            else -> "Disconnected"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                when {
                    isConnected -> {
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Disconnect",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Close")
                        }
                    }
                    isDisconnected && !isChecking -> {
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Connect",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Connect")
                        }
                    }
                    isChecking -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val connectionViewModel: ConnectionViewModel = viewModel()
    val connectionState by connectionViewModel.connectionState.collectAsState()
    val isChecking by connectionViewModel.isChecking.collectAsState()
    
    val isBannerCollapsed = false  // Banner always visible
    
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBanner = currentRoute == "visual"

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("TruVision") }
                )
                if (showBanner) {
                    ConnectionStatusBanner(
                        connectionState = connectionState,
                        isChecking = isChecking,
                        onRetry = { connectionViewModel.checkConnection() },
                        onClose = { connectionViewModel.disconnect() },
                        isCollapsed = isBannerCollapsed
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "visual",
                    onClick = { navController.navigate("visual") { launchSingleTop = true } },
                    icon = { Icon(Icons.Filled.CameraAlt, contentDescription = "Visual") },
                    label = { Text("Visual") }
                )
                NavigationBarItem(
                    selected = currentRoute == "history",
                    onClick = { navController.navigate("history") { launchSingleTop = true } },
                    icon = { Icon(Icons.Filled.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") { launchSingleTop = true } },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "visual",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("results/{job_id}") { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("job_id") ?: ""
                ResultsScreen(jobId = jobId)
            }
            composable("results") { ResultsScreen(jobId = null) }
            composable("visual") { VisualScreen(navController = navController) }
            composable("analysis") { AnalysisScreen() }
            composable("history") { HistoryScreen(navController) }
            composable("settings") { SettingsScreen() }
            composable("connection") { ConnectionScreen() }
        }
    }
}

@Composable
fun ResultsScreen(jobId: String?) {
    val viewModel: ResultsViewModel = viewModel()
    val jobState by viewModel.jobState.collectAsState()
    
    LaunchedEffect(jobId) {
        if (jobId != null && jobId.isNotEmpty()) {
            viewModel.startPolling(jobId)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Detection Results", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (jobId == null || jobId.isEmpty()) {
            Text(
                "No active job. Start a capture from Visual screen.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            when {
                jobState.error != null -> {
                    var showErrorDialog by remember { mutableStateOf(true) }
                    
                    if (showErrorDialog) {
                        com.truvision.app.ui.common.ErrorDialog(
                            title = "Detection Failed",
                            message = jobState.error ?: "Unknown error occurred",
                            onDismiss = { showErrorDialog = false },
                            onRetry = { viewModel.retryPolling() },
                            showRetry = true
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Detection Failed",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            jobState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retryPolling() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
                jobState.isPolling && jobState.status == "running" -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Processing microplastic detection...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Elapsed time: ${jobState.elapsedSeconds}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                jobState.status == "completed" -> {
                    val count = jobState.detectedCount ?: 0
                    val riskLevel = when {
                        count <= 3 -> "Low"
                        count <= 6 -> "Medium"
                        else -> "High"
                    }
                    val riskColor = when (riskLevel) {
                        "Low" -> Color(0xFF4CAF50)
                        "Medium" -> Color(0xFFFFA726)
                        else -> Color(0xFFF44336)
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "$count",
                                style = MaterialTheme.typography.displayLarge,
                                color = riskColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Microplastic Particles Detected",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Risk Level: $riskLevel",
                                style = MaterialTheme.typography.titleMedium,
                                color = riskColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val detections = jobState.detections ?: emptyList()
                    if (detections.isNotEmpty()) {
                        val polymerBreakdown = detections.groupingBy { it.polymerType }.eachCount()
                        
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Polymer Breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                polymerBreakdown.entries.sortedByDescending { it.value }.forEach { (polymer, count) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("$count× $polymer", style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Individual Detections",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        detections.forEach { detection ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            detection.polymerType,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Confidence: ${(detection.confidence * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        "#${detection.id}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Metadata",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Job ID: ${jobState.jobId}", style = MaterialTheme.typography.bodySmall)
                            Text("Processing Time: ${jobState.elapsedSeconds}s", style = MaterialTheme.typography.bodySmall)
                            if (jobState.imagePath != null) {
                                Text("Image: ${jobState.imagePath}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Week 10 - Export Button
                    Button(
                        onClick = { 
                            viewModel.exportJob(jobState.jobId ?: "")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export Sample")
                    }
                }
                else -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Initializing...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun VisualScreen(navController: androidx.navigation.NavController) {
    val viewModel: VisualViewModel = viewModel()
    val captureState by viewModel.captureState.collectAsState()
    
    val toastState = com.truvision.app.ui.common.rememberToastState()
    var selectedTab by remember { mutableStateOf(0) }
    var preloadedImage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(captureState) {
        if (captureState is CaptureState.Success) {
            val jobId = (captureState as CaptureState.Success).jobId
            navController.navigate("results/$jobId")
            toastState.show("Capture started successfully!", com.truvision.app.ui.common.ToastType.SUCCESS)
            viewModel.resetState()
        }
    }
    
    LaunchedEffect(captureState) {
        val state = captureState
        if (state is CaptureState.Error) {
            toastState.show(state.message, com.truvision.app.ui.common.ToastType.ERROR)
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            viewModel.loadSamples()
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { 
                    Text(
                        "Capture",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    ) 
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { 
                    Text(
                        "Browse",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    ) 
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { 
                    Text(
                        "Analyze",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                    ) 
                }
            )
        }
        
        when (selectedTab) {
            0 -> {
                com.truvision.app.ui.visual.CaptureTab(
                    viewModel = viewModel
                )
            }
            1 -> {
                com.truvision.app.ui.visual.BrowseTab(
                    onAnalyzeClick = { imageId ->
                        preloadedImage = imageId
                        selectedTab = 2
                    }
                )
            }
            2 -> {
                com.truvision.app.ui.visual.AnalyzeTab(
                    preloadedImage = preloadedImage
                )
            }
        }
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.BottomCenter
        ) {
            com.truvision.app.ui.common.ToastHost(toastState = toastState)
        }
    }
}


@Composable
fun AnalysisScreen() {
    ScreenPlaceholder(title = "Analysis", content = "Analysis tools and charts will appear here.")
}

@Composable
fun SampleCard(
    sample: SampleResponse,
    onExportClick: () -> Unit,
    onClick: () -> Unit
) {
    val riskLevel = when (sample.detectedCount ?: 0) {
        in 30..Int.MAX_VALUE -> "High Risk" to Color(0xFFE57373)
        in 15..29 -> "Medium Risk" to Color(0xFFFFA726)
        else -> "Low Risk" to Color(0xFF66BB6A)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_my_calendar),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        sample.timestamp ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = riskLevel.second
                ) {
                    Text(
                        riskLevel.first,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_mylocation),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    sample.captureMethod ?: "Lab Sample",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Particles: ${sample.detectedCount ?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Primary: ${if ((sample.detectedCount ?: 0) > 0) sample.primaryPolymer ?: "PET" else "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            if (sample.latitude != null && sample.longitude != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Location: ${String.format("%.4f", sample.latitude)}N, ${String.format("%.4f", sample.longitude)}E",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (sample.status == "exported") {
                Text(
                    "Exported",
                    modifier = Modifier
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onExportClick,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_save),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export")
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(navController: androidx.navigation.NavController){
    val viewModel: HistoryViewModel = viewModel()
    val historyState by viewModel.historyState.collectAsState()
    val toastState = com.truvision.app.ui.common.rememberToastState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("History", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        when (val state = historyState) {
            is HistoryState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(5) {
                        com.truvision.app.ui.common.SkeletonLoader()
                    }
                }
            }
            is HistoryState.Success -> {
                if (state.samples.isEmpty()) {
                    Text(
                        "No samples yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            SummaryCardsRow(samples = state.samples)
                        }
                        
                        item {
                            FilterRow()
                        }
                        
                        items(state.samples.size) { index ->
                            val sample = state.samples[index]
                            SampleCard(
                                sample = sample,
                                onExportClick = {
                                    val jobId = sample.jobId ?: ""
                                    if (jobId.isNotEmpty()) {
                                        viewModel.exportSample(jobId) { success, message ->
                                            if (success) {
                                                toastState.show(message, com.truvision.app.ui.common.ToastType.SUCCESS)
                                            } else {
                                                toastState.show(message, com.truvision.app.ui.common.ToastType.ERROR)
                                            }
                                        }
                                    } else {
                                        toastState.show("Missing job id for export", com.truvision.app.ui.common.ToastType.ERROR)
                                    }
                                },
                                onClick = {
                                    navController.navigate("results/${sample.jobId}")
                                }
                            )
                        }
                    }
                }
            }
            is HistoryState.Error -> {
                var showErrorDialog by remember { mutableStateOf(true) }
                
                if (showErrorDialog) {
                    com.truvision.app.ui.common.ErrorDialog(
                        title = "Failed to Load History",
                        message = state.message,
                        onDismiss = { showErrorDialog = false },
                        onRetry = { viewModel.fetchSamples() },
                        showRetry = true
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Failed to Load History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchSamples() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
            }
        }
    }
}


@Composable
fun SummaryCardsRow(samples: List<com.truvision.app.api.SampleResponse>) {
    val totalSamples = samples.size
    val highRisk = samples.count { (it.detectedCount ?: 0) > 20 }
    val exported = samples.count { it.status == "exported" }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SummaryCard(value = totalSamples, label = "Total Samples", color = Color(0xFF2196F3))
        SummaryCard(value = highRisk, label = "High Risk", color = Color(0xFFFFA726))
        SummaryCard(value = exported, label = "Exported", color = Color(0xFF66BB6A))
    }
}

@Composable
fun SummaryCard(value: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun FilterRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(text = "All Risk", modifier = Modifier.weight(1f))
        FilterChip(text = "By Date", modifier = Modifier.weight(1f))
    }
}

@Composable
fun FilterChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, style = MaterialTheme.typography.bodyMedium)
            Icon(
                painter = painterResource(android.R.drawable.arrow_down_float),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val overridePrefs = remember { com.truvision.app.connectivity.OverridePreferences(context) }
    val connectionViewModel: ConnectionViewModel = viewModel()
    val connectionState by connectionViewModel.connectionState.collectAsState()
    
    var isOverrideEnabled by remember { mutableStateOf(overridePrefs.isOverrideEnabled()) }
    var overrideUrl by remember { mutableStateOf(overridePrefs.getOverrideUrl()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Advanced", style = MaterialTheme.typography.titleLarge)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mode: ${if (isOverrideEnabled) "Override" else "Default (auto USB discovery)"}")
            Switch(
                checked = isOverrideEnabled,
                onCheckedChange = { enabled ->
                    isOverrideEnabled = enabled
                    overridePrefs.setOverrideEnabled(enabled)
                }
            )
        }
        
        if (isOverrideEnabled) {
            OutlinedTextField(
                value = overrideUrl,
                onValueChange = { newUrl ->
                    overrideUrl = newUrl
                    overridePrefs.setOverrideUrl(newUrl)
                },
                label = { Text("Override Base URL") },
                placeholder = { Text("http://192.168.231.240:8000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(
                text = "When Override is ON, all requests will use this URL instead of auto-discovery.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Connection Diagnostics", style = MaterialTheme.typography.titleLarge)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DiagnosticRow("Base URL", connectionState.baseUrl ?: "--")
                DiagnosticRow("Last HTTP Code", connectionState.httpCode?.toString() ?: "--")
                DiagnosticRow("Latency", connectionState.latencyMs?.let { "${it}ms" } ?: "--")
                DiagnosticRow("Status", connectionState.status)
            }
        }
    }
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}
@Composable
fun ConnectionScreen() {
    val viewModel: ConnectionViewModel = viewModel()
    val connectionState by viewModel.connectionState.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Connection", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        
        if (connectionState.status == "Connected") {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Connected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Connected",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Not connected",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Not connected",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { viewModel.checkConnection() },
            enabled = !isChecking
        ) {
            Text(if (isChecking) "Checking..." else "Retry Connection")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "See Settings → Advanced for diagnostics",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ScreenPlaceholder(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content)
    }
}
