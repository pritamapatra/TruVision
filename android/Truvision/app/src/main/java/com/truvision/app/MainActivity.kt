package com.truvision.app
import com.truvision.app.BuildConfig

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.truvision.app.ui.theme.TruVisionTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.truvision.app.connectivity.ConnectionViewModel
import com.truvision.app.visual.VisualViewModel
import com.truvision.app.visual.CaptureState
import com.truvision.app.results.ResultsViewModel
import com.truvision.app.results.JobState
import androidx.lifecycle.viewmodel.compose.viewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TruVision") }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "connection",
                    onClick = { navController.navigate("connection") { launchSingleTop = true } },
                    icon = { Text("📱") },
                    label = { Text("Connect") }
                )
                NavigationBarItem(
                    selected = currentRoute == "visual",
                    onClick = { navController.navigate("visual") { launchSingleTop = true } },
                    icon = { Text("📷") },
                    label = { Text("Visual") }
                )
                NavigationBarItem(
                    selected = currentRoute == "results",
                    onClick = { navController.navigate("results") { launchSingleTop = true } },
                    icon = { Text("📊") },
                    label = { Text("Results") }
                )
                NavigationBarItem(
                    selected = currentRoute == "history",
                    onClick = { navController.navigate("history") { launchSingleTop = true } },
                    icon = { Text("📜") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") { launchSingleTop = true } },
                    icon = { Text("⚙️") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "connection",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("results/{job_id}") { backStackEntry ->
                val jobId = backStackEntry.arguments?.getString("job_id") ?: ""
                ResultsScreen(jobId = jobId)
            }
            composable("results") { ResultsScreen(jobId = null) }
            composable("visual") { VisualScreen(navController = navController) }
            composable("analysis") { AnalysisScreen() }
            composable("history") { HistoryScreen() }
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Results", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (jobId == null || jobId.isEmpty()) {
            Text(
                "No active job. Start a capture from Visual screen.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text("Job ID: ${jobState.jobId ?: jobId}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            when {
                jobState.error != null -> {
                    Text(
                        "Error: ${jobState.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retryPolling() }) {
                        Text("Retry")
                    }
                }
                jobState.isPolling -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Status: ${jobState.status}")
                    Text("Polling for updates...", style = MaterialTheme.typography.bodySmall)
                }
                jobState.status == "completed" -> {
                    Text("Status: Completed", color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Detected Count: ${jobState.detectedCount ?: 0}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                else -> {
                    Text("Status: ${jobState.status}")
                }
            }
        }
    }
}



@Composable
fun VisualScreen(navController: androidx.navigation.NavController) {
    val viewModel: VisualViewModel = viewModel()
    val captureState by viewModel.captureState.collectAsState()
    
    LaunchedEffect(captureState) {
        if (captureState is CaptureState.Success) {
            val jobId = (captureState as CaptureState.Success).jobId
            navController.navigate("results/$jobId")
            viewModel.resetState()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Visual Capture", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        when (val state = captureState) {
            is CaptureState.Idle -> {
                Text(
                    "Ready to capture microplastic sample",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is CaptureState.Starting -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Starting capture...")
            }
            is CaptureState.Error -> {
                Text(
                    state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.resetState() }) {
                    Text("Dismiss")
                }
            }
            is CaptureState.Success -> {
                Text("Navigating to results...")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.startCapture() },
            modifier = Modifier.fillMaxWidth(0.6f),
            enabled = captureState is CaptureState.Idle
        ) {
            Text("Start Capture")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Flow: Visual -> POST /capture/start -> Navigate to Results -> Poll job",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun AnalysisScreen() {
    ScreenPlaceholder(title = "Analysis", content = "Analysis tools and charts will appear here.")
}

@Composable
fun HistoryScreen() {
    ScreenPlaceholder(title = "History", content = "Sample history will be listed here.")
}

@Composable
fun SettingsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val overridePrefs = remember { com.truvision.app.connectivity.OverridePreferences(context) }
    
    var isOverrideEnabled by remember { mutableStateOf(overridePrefs.isOverrideEnabled()) }
    var overrideUrl by remember { mutableStateOf(overridePrefs.getOverrideUrl()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
        Text("Connection Status", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Status: ${connectionState.status}")
        Text("Last HTTP code: ${connectionState.httpCode?.toString() ?: "--"}")
        Text("Base URL: ${connectionState.baseUrl ?: "--"}")
        Text("Latency: ${connectionState.latencyMs?.let { "$it ms" } ?: "--"}")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.checkConnection() },
            enabled = !isChecking
        ) {
            Text(if (isChecking) "Checking..." else "Run USB health check")
        }
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
