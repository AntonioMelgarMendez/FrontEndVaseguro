package com.VaSeguro

import android.os.Bundle
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.VaSeguro.ui.components.Container.GeneralScaffold.GeneralScaffold
import com.VaSeguro.ui.screens.Start.Login.LoginScreen
import com.VaSeguro.ui.screens.Start.SignUp.SignUpScreen
import com.VaSeguro.ui.screens.Start.Starting.StartingScreen
import com.VaSeguro.ui.screens.Start.SplashScren.SplashScreen
import com.VaSeguro.ui.theme.VaSeguroTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowInsetsControllerCompat
import com.VaSeguro.ui.Aux.ContentScreen
import com.VaSeguro.ui.navigations.MainNavigation
import com.VaSeguro.ui.screens.Start.CreateAccountDriver.CreateAccountDriverScreen
import com.VaSeguro.ui.screens.Start.CreateAccountDriver.RegisterBus.RegisterBusScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.statusBarColor = "#FEF7FF".toColorInt()
        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = true
        setContent {
            VaSeguroTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    val navController = rememberNavController()
//                    MainNavigation(navController = navController, false)

                    val navController = rememberNavController()

                    AnimatedNavHost(
                        navController = navController,
                        startDestination = "splash",
                        enterTransition = { slideInHorizontally() + fadeIn() },
                        exitTransition = { slideOutHorizontally() + fadeOut() },
                        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                    ) {
                        composable("splash") {
                            SplashScreen(navController)
                        }
                        composable("home") {
                            GeneralScaffold(navController)
                        }
                        composable("starting") {
                            StartingScreen(navController)
                        }
                        composable("login") {
                            LoginScreen(navController)
                        }
                        composable("signup") {
                            SignUpScreen(navController)
                        }
                        composable("driver_registration"){
                            CreateAccountDriverScreen(navController)
                        }
                        composable("code") { }
                        composable(
                            route = "content/{message}/{description}/{imageRes}/{buttonText}/{destination}",
                        ) { backStackEntry ->
                            val message = backStackEntry.arguments?.getString("message") ?: ""
                            val description = backStackEntry.arguments?.getString("description") ?: ""
                            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.ic_launcher_foreground
                            val buttonText = backStackEntry.arguments?.getString("buttonText") ?: "Continue"
                            val destination = backStackEntry.arguments?.getString("destination") ?: "home"
                            ContentScreen(
                                message = message,
                                description = description,
                                imageRes = imageRes,
                                buttonText = buttonText,
                                navController = navController,
                                destination = destination
                            )
                        }
                        composable("save_bus") {
                            RegisterBusScreen(navController,{})
                        }
                    }
                }
            }
        }
    }
}

//val navController = rememberNavController()
//
//// Usamos el sistema de navegación principal definido en MainNavigation
//MainNavigation(navController = navController)