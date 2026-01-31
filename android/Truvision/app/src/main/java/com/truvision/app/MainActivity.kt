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
import com.truvision.app.ui.theme.TruVisionTheme

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TruVision") }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "settings",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("results") { ResultsScreen() }
            composable("visual") { VisualScreen() }
            composable("analysis") { AnalysisScreen() }
            composable("history") { HistoryScreen() }
            composable("settings") { SettingsScreen() }
            composable("connection") { ConnectionScreen() }
        }
    }
}

@Composable
fun ResultsScreen() {
    ScreenPlaceholder(title = "Results", content = "Detection results will be displayed here.")
}

@Composable
fun VisualScreen() {
    ScreenPlaceholder(title = "Visual", content = "Visual capture interface will be here.")
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val discovery = remember { com.truvision.app.connectivity.UsbIpDiscovery() }
    val prefs = remember { com.truvision.app.connectivity.UsbPreferences(context) }
    val overridePrefs = remember { com.truvision.app.connectivity.OverridePreferences(context) }
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf("Not connected") }
    var lastCode by remember { mutableStateOf<Int?>(null) }
    var baseUrl by remember { mutableStateOf<String?>(null) }
    var latencyMs by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Connection Status", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Status: $status")
        Text("Last HTTP code: ${lastCode?.toString() ?: "--"}")
        Text("Base URL: ${baseUrl ?: "--"}")
        Text("Latency: ${latencyMs?.let { "$it ms" } ?: "--"}")
        Spacer(modifier = Modifier.height(24.dp))
        
        if (BuildConfig.DEBUG) {
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Diagnostics (Debug Only)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Last health status: $status", style = MaterialTheme.typography.bodySmall)
                    Text("Last HTTP code: ${lastCode?.toString() ?: "--"}", style = MaterialTheme.typography.bodySmall)
                    Text("Last base URL: ${baseUrl ?: "--"}", style = MaterialTheme.typography.bodySmall)
                    Text("Last latency: ${latencyMs?.let { "$it ms" } ?: "--"}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(onClick = {
            status = "Checking..."
            lastCode = null
            baseUrl = null
            latencyMs = null

            scope.launch {
                val isOverride = overridePrefs.isOverrideEnabled()
                
                if (isOverride) {
                    val overrideUrl = overridePrefs.getOverrideUrl()
                    val startTime = System.currentTimeMillis()
                    
                    try {
                        val url = "$overrideUrl/health"
                        val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                        conn.apply {
                            requestMethod = "GET"
                            instanceFollowRedirects = false
                            connectTimeout = 2000
                            readTimeout = 2000
                        }
                        conn.connect()
                        val code = conn.responseCode
                        conn.disconnect()
                        
                        lastCode = code
                        latencyMs = System.currentTimeMillis() - startTime
                        
                        if (code in 200..299) {
                            status = "Connected"
                            baseUrl = overrideUrl
                        } else {
                            status = "Not connected"
                            baseUrl = overrideUrl
                        }
                    } catch (e: Exception) {
                        latencyMs = System.currentTimeMillis() - startTime
                        status = "Not connected"
                        lastCode = null
                        baseUrl = overrideUrl
                    }
                } else {
                    val lastKnownIp = prefs.getLastSuccessfulIp()
                    val result = discovery.discoverUsbBaseUrl(lastKnownIp)
                    
                    lastCode = result.httpCode
                    latencyMs = result.latencyMs
                    
                    if (result.baseUrl != null && result.httpCode != null && result.httpCode in 200..299) {
                        status = "Connected"
                        baseUrl = result.baseUrl
                        
                        val ip = result.baseUrl.replace("http://", "").replace(":8000", "")
                        prefs.saveLastSuccessfulIp(ip)
                    } else {
                        status = "Not connected"
                        baseUrl = result.baseUrl
                    }
                }
            }
        }) {
            Text("Run USB health check")
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
