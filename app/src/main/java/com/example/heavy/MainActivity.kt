package com.example.heavy

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.heavy.service.StressTestService
import com.example.heavy.ui.theme.HeavyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HeavyTheme {
                    MainScreen(context = LocalContext.current)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HeavyTheme {
        Greeting("Android")
    }
}

@Composable
fun MainScreen(context: Context) {
    // State variables to hold user input for CPU and RAM percentages
    var cpuPercent by remember { mutableStateOf("80") } // Default value set to 80
    var ramPercent by remember { mutableStateOf("60") } // Default value set to 60

    // Box to center the content
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Column to hold the buttons and inputs
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Space between items
        ) {
            // Text Field for CPU percentage input
            OutlinedTextField(
                value = cpuPercent,
                onValueChange = { cpuPercent = it },
                label = { Text("CPU Percentage") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(0.8f) // Adjust width
            )

            // Text Field for RAM percentage input
            OutlinedTextField(
                value = ramPercent,
                onValueChange = { ramPercent = it },
                label = { Text("RAM Percentage") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(0.8f) // Adjust width
            )

            // Start Button
            Button(onClick = {
                // Get the CPU and RAM percentage from the TextFields and pass them to the service
                val cpu = cpuPercent.toIntOrNull() ?: 80  // Default to 80 if not valid
                val ram = ramPercent.toIntOrNull() ?: 60  // Default to 60 if not valid

                val intent = Intent(context, StressTestService::class.java).apply {
                    putExtra(StressTestService.EXTRA_CPU_PERCENT, cpu)
                    putExtra(StressTestService.EXTRA_RAM_PERCENT, ram)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }) {
                Text(text = "Start Stress Test")
            }

            // Stop Button
            Button(onClick = {
                val intent = Intent(context, StressTestService::class.java)
                context.stopService(intent)
            }) {
                Text(text = "Stop Stress Test")
            }
        }
    }
}


