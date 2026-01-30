package com.truvision.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
            startDestination = "connection",
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
    ScreenPlaceholder(title = "Settings", content = "App settings and configuration options.")
}

@Composable
fun ConnectionScreen() {
    val resolver = remember { com.truvision.app.connectivity.ConnectionResolver() }
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf("Not connected") }
    var lastCode by remember { mutableStateOf<Int?> (null) }
    var baseUrl by remember { mutableStateOf<String?> (null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Connection Status", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Status: " + status)
        Text("Last HTTP code: " + (lastCode?.toString() ?: "--"))
        Text("Base URL: " + (baseUrl ?: "--"))
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            status = "Checking..."
            lastCode = null
            baseUrl = null

            scope.launch {
                val result = resolver.resolveUsbBaseUrl()
                lastCode = result.httpCode
                if (result.baseUrl != null && result.httpCode != null && result.httpCode in 200..299) {
                    status = "Connected"
                    baseUrl = result.baseUrl
                } else {
                    status = "Not connected"
                    baseUrl = result.baseUrl
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
