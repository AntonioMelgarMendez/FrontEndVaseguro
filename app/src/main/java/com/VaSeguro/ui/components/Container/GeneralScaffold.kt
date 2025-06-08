package com.VaSeguro.ui.components.Container

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.VaSeguro.ui.navigations.BusScreenNavigation
import com.VaSeguro.ui.navigations.ChildrenScreenNavigation
import com.VaSeguro.ui.navigations.HistoryScreenNavigation
import com.VaSeguro.ui.navigations.MainNavigation
import com.VaSeguro.ui.navigations.MapScreenNavigation

@Composable
fun GeneralScaffold() {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val navController = rememberNavController()
    var title by remember { mutableStateOf("Map") }
    var selectedItem by remember { mutableStateOf("Map") }

    val navItems = listOf("Map", "History", "Bus", "Children")

    fun onItemSelected(currentItem: String) {
        selectedItem = currentItem
        title = currentItem

        when (currentItem) {
            "Map" -> navController.navigate(MapScreenNavigation)
            "History" -> navController.navigate(HistoryScreenNavigation)
            "Bus" -> navController.navigate(BusScreenNavigation)
            "Children" -> navController.navigate(ChildrenScreenNavigation)
        }
    }

    Scaffold(
        topBar = { TopBar(title = title) },
        bottomBar = {
            BottomBar(
                selectedItem = selectedItem,
                onItemSelected = { onItemSelected(it) },
                navItems = navItems
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MainNavigation(navController = navController)
        }
    }
}
