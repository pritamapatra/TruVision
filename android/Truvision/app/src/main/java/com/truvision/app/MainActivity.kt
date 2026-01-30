package com.truvision.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.truvision.app.connectivity.ConnectionResolver
import com.truvision.app.ui.theme.TruVisionTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TruVisionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var status by remember { mutableStateOf("Not checked") }
                    var baseUrl by remember { mutableStateOf<String?>(null) }
                    var httpCode by remember { mutableStateOf<Int?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Week 0 USB Connectivity Check")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Status: $status")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Resolved base URL: ${baseUrl ?: "-"}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "HTTP code: ${httpCode ?: "-"}")
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = {
                            status = "Checking..."
                            baseUrl = null
                            httpCode = null
                            CoroutineScope(Dispatchers.Main).launch {
                                val resolver = ConnectionResolver()
                                val result = resolver.resolveUsbBaseUrl()
                                baseUrl = result.baseUrl
                                httpCode = result.httpCode
                                status = if (result.baseUrl != null) {
                                    "OK"
                                } else {
                                    "Failed"
                                }
                            }
                        }) {
                            Text(text = "Run USB health check")
                        }
                    }
                }
            }
        }
    }
}
