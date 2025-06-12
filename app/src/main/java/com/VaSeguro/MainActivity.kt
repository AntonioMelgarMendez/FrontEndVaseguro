package com.VaSeguro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.VaSeguro.ui.components.Container.GeneralScaffold
import com.VaSeguro.ui.navigations.RouteScreenNavigation
import com.VaSeguro.ui.screens.Driver.Route.RouteScreen
import com.VaSeguro.ui.screens.Start.Login.LoginScreen
import com.VaSeguro.ui.screens.Start.SignUp.SignUpScreen
import com.VaSeguro.ui.screens.Start.Starting.StartingScreen
import com.VaSeguro.ui.screens.Utils.SplashScreen
import com.VaSeguro.ui.theme.VaSeguroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = RouteScreenNavigation) {
                composable("splash") {
                    SplashScreen(navController)
                }
                composable("home") {
                    GeneralScaffold()
                }
                composable("starting"){
                    StartingScreen(navController)
                }
                composable("login"){
                    LoginScreen(navController)
                }
                composable("signup"){
                    SignUpScreen(navController)
                }
                composable("code"){

                }
                composable<RouteScreenNavigation> { RouteScreen() }
            }
        }
    }
}

