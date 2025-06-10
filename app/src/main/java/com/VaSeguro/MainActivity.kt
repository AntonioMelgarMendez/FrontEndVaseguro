package com.VaSeguro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.VaSeguro.ui.screens.Admin.Vehicle.VehicleScreen
import com.VaSeguro.ui.theme.VaSeguroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaSeguroTheme {
                VehicleScreen()
            }
        }
    }
}

