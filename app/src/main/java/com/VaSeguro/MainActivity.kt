package com.VaSeguro

import android.os.Bundle
import android.view.WindowInsets.Type.navigationBars
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.VaSeguro.ui.components.Container.GeneralScaffold
import com.VaSeguro.ui.screens.Admin.Vehicle.VehicleScreen
import com.VaSeguro.ui.screens.Parents.Children.ChildrenScreen
import com.VaSeguro.ui.screens.Start.Code.CodeScreen
import com.VaSeguro.ui.screens.Start.Login.LoginScreen
import com.VaSeguro.ui.screens.Start.SignUp.SignUpScreen
import com.VaSeguro.ui.screens.Start.Starting.StartingScreen
import com.VaSeguro.ui.screens.Utils.SplashScreen
import com.VaSeguro.ui.theme.VaSeguroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            VaSeguroTheme(darkTheme = false){
                ChildrenScreen()
            }
        }
    }

}

