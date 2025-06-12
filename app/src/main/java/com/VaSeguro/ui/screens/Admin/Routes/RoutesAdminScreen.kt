package com.VaSeguro.ui.screens.Admin.Routes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.VaSeguro.data.model.Routes.RouteStatus
import com.VaSeguro.data.model.Routes.RouteType
import com.VaSeguro.data.model.Stop.StopType
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.components.Container.DropDownSelector
import com.VaSeguro.ui.components.Container.ExpandableInfoCard
import com.VaSeguro.ui.screens.Admin.Stops.StopsAdminScreen
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.navigation.compose.rememberNavController
import com.VaSeguro.ui.components.Container.TopBarContainer.TopBar
import com.VaSeguro.ui.theme.PrimaryColor

@Composable
fun RoutesAdminScreen(
    viewModel: RoutesAdminScreenViewModel = viewModel()
){
    val routes by viewModel.routes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var routeToDelete by remember { mutableStateOf<String?>(null) }

        LazyColumn() {
            items(routes) { route ->
                ExpandableInfoCard(
                    id = route.id,
                    title = route.name,
                    info = listOf(
                        "Start Date" to route.start_date,
                        "End Date" to route.end_date,
                        "Vehicle ID" to route.vehicule_id,
                        "Status" to route.status_id.status,
                        "Type" to route.type_id.type
                    ),
                    onEdit = { /* future edit */ },
                    onDelete = { routeToDelete = route.id }
                )
            }
        }

        if (showAddDialog) {
            AddRouteDialog(
                onDismiss = { showAddDialog = false },
                onSave = { showAddDialog = false }
            )
        }

        routeToDelete?.let {
            ConfirmationDialog(
                message = "Are you sure you want to delete this route?",
                onConfirm = {
                    viewModel.deleteRoute(it)
                    routeToDelete = null
                },
                onDismiss = { routeToDelete = null }
            )
        }

}

@Composable
fun AddRouteDialog(
    viewModel: RoutesAdminScreenViewModel = viewModel(),
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var startDate by remember { mutableStateOf(TextFieldValue("")) }
    var endDate by remember { mutableStateOf(TextFieldValue("")) }
    var vehiculeId by remember { mutableStateOf(TextFieldValue("")) }
    var routeType by remember { mutableStateOf<RouteType?>(null) }
    var routeStatus by remember { mutableStateOf<RouteStatus?>(null) }

    val routeTypes = listOf(
        RouteType("1", "Long Distance"),
        RouteType("2", "Short Distance")
    )

    val routeStatuses = listOf(
        RouteStatus("1", "Active"),
        RouteStatus("2", "Inactive")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Route") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Route Name") })
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Date") })
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End Date") })
                OutlinedTextField(value = vehiculeId, onValueChange = { vehiculeId = it }, label = { Text("Vehicule ID") })

                DropDownSelector("Route Type", routeTypes.map { it.type }, routeType?.type) { selectedType ->
                    routeType = routeTypes.find { it.type == selectedType }
                }
                DropDownSelector("Route Status", routeStatuses.map { it.status }, routeStatus?.status) { selectedStatus ->
                    routeStatus = routeStatuses.find { it.status == selectedStatus }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (routeType != null && routeStatus != null) {
                    viewModel.addRoute(
                        name.text,
                        startDate.text,
                        vehiculeId.text,
                        routeStatus!!,
                        routeType!!,
                        endDate.text
                    )
                    onSave()
                }
            },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = PrimaryColor
                )
            ) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(2.dp, PrimaryColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryColor
                )
            ) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun RoutesAdminPreview() {
    RoutesAdminScreen()
}