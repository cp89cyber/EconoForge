package com.econoforge.simulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.econoforge.simulator.ui.simulator.SimulatorRoute
import com.econoforge.simulator.ui.theme.EconoForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EconoForgeTheme {
                SimulatorRoute()
            }
        }
    }
}
