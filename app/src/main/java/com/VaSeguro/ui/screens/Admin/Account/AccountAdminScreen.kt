
package com.VaSeguro.ui.screens.Admin.Account

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
fun AccountAdminScreen(viewModel: AccountAdminScreenViewModel = viewModel()) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val userState by viewModel.userData.collectAsState()
    val securityState by viewModel.securityState.collectAsState()

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 2.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("NADA CAMBIA")
        when (selectedTabIndex) {
            0 -> AccountFormSection(
                state = userState,
                original = viewModel.originalUserData,
                onValueChange = viewModel::onUserFieldChange,
                onUpdate = viewModel::onUpdateAccount,
                onCancel = viewModel::onCancelChanges,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
            1 -> SecurityFormSection(
                state = securityState,
                original = viewModel.originalSecurityState,
                onValueChange = viewModel::onSecurityFieldChange,
                onUpdate = viewModel::onUpdatePassword,
                onCancel = viewModel::onCancelChanges,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConfigurationScreen() {
    AccountAdminScreen()
}