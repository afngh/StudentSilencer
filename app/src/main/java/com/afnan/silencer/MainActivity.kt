package com.afnan.silencer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.afnan.silencer.data.RingerMode
import com.afnan.silencer.service.RingerModeController
import com.afnan.silencer.ui.onboarding.PermissionOnboardingScreen
import com.afnan.silencer.ui.theme.SilencerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SilencerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        var showTestUI by remember { mutableStateOf(false) }

                        if (!showTestUI) {
                            PermissionOnboardingScreen(
                                onContinue = { showTestUI = true }
                            )
                        } else {
                            TestRingerScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestRingerScreen() {
    val context = LocalContext.current
    val controller = remember { RingerModeController(context) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Test Ringer Mode Controller", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = { controller.setMode(RingerMode.SILENT) }) {
            Text("Set to SILENT")
        }
        Button(onClick = { controller.setMode(RingerMode.VIBRATE) }) {
            Text("Set to VIBRATE")
        }
        Button(onClick = { controller.setMode(RingerMode.DND) }) {
            Text("Set to DND")
        }
        Button(onClick = { controller.setMode(RingerMode.NORMAL) }) {
            Text("Set to NORMAL")
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Welcome to SilentScheduler!\nScaffolding is ready.",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SilencerTheme {
        HomeScreen()
    }
}