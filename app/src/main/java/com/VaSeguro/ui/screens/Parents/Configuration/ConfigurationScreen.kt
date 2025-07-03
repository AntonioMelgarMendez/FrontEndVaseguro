package com.VaSeguro.ui.screens.Parents.Configuration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.data.AppProvider
import com.VaSeguro.ui.components.Forms.SegmentedTabRow

@Composable
fun ConfigurationScreen() {
    val context = LocalContext.current
    val viewModel: ConfigurationScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return ConfigurationScreenViewModel(
                    appProvider.provideUserPreferences(),
                    appProvider.provideAuthRepository()
                ) as T
            }
        }
    )
    val isLoading by viewModel.isLoading.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val userState by viewModel.userData.collectAsState()
    val securityState by viewModel.securityState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        SegmentedTabRow(
            selectedIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTabIndex) {
            0 -> AccountFormSection(
                state = userState,
                original = viewModel.originalUserData,
                onValueChange = viewModel::onUserFieldChange,
                onUpdate ={ viewModel.onUpdateAccount(context)},
                onCancel = viewModel::onCancelChanges,
                isLoading = isLoading,
                updateSuccess = updateSuccess,
                onDismissSuccess = { viewModel.resetUpdateSuccess() }
            )
            1 -> SecurityFormSection(
                state = securityState,
                original = viewModel.originalSecurityState,
                onValueChange = viewModel::onSecurityFieldChange,
                onUpdate = { viewModel.changePassword(securityState.oldPassword, securityState.newPassword) },
                onCancel = viewModel::onCancelChanges,
                isLoading = isLoading,
                updateSuccess = updateSuccess,
                onDismissSuccess = { viewModel.resetUpdateSuccess() }
            )
        }
    }
}