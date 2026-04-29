package com.example.acremotecontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.acremotecontrol.ui.theme.ACRemoteControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsManager = PreferencesManager(applicationContext)

        setContent {
            ACRemoteControlTheme {
                var imei by remember { mutableStateOf(prefsManager.getImei()) }
                var showRemote by remember { mutableStateOf(false) }

                if (showRemote && imei != null) {
                    RemoteControlScreen(imei = imei!!)
                } else {
                    ImeiScreen(
                        savedImei = imei,
                        onConfirm = { input ->
                            prefsManager.saveImei(input)
                            imei = input
                            showRemote = true
                        }
                    )
                }
            }
        }
    }
}
