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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.VaSeguro.ui.components.Forms.SegmentedTabRow

@Composable
fun ConfigurationScreen( viewModel: ConfigurationScreenViewModel = viewModel()) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val userState by viewModel.userData.collectAsState()
    val securityState by viewModel.securityState.collectAsState()

    Column(modifier = Modifier.fillMaxSize() .background(Color.White)) {

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
                onUpdate = viewModel::onUpdateAccount,
                onCancel = viewModel::onCancelChanges
            )
            1 -> SecurityFormSection(
                state = securityState,
                original = viewModel.originalSecurityState,
                onValueChange = viewModel::onSecurityFieldChange,
                onUpdate = viewModel::onUpdatePassword,
                onCancel = viewModel::onCancelChanges
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewConfigurationScreen() {
    ConfigurationScreen()
}