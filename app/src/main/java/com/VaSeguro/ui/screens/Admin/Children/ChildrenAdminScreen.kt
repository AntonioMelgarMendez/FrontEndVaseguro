package com.VaSeguro.ui.screens.Admin.Children

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.VaSeguro.data.AppProvider
import com.VaSeguro.data.model.Child.toChildren
import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.ui.components.AddDialogues.AddChildDialogAdmin
import com.VaSeguro.ui.components.Cards.AdminCardItem
import com.VaSeguro.ui.components.Container.ConfirmationDialog
import com.VaSeguro.ui.theme.PrimaryColor
import kotlin.collections.lastIndex


@Composable
fun ChildrenAdminScreen() {
    val context = LocalContext.current
    val viewModel: ChildrenAdminScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return ChildrenAdminScreenViewModel(
                    appProvider.provideChildrenRepository(),
                    appProvider.provideUserPreferences(),
                    appProvider.provideAuthRepository(),
                    appProvider.provideStopsRepository(),
                    appProvider.provideChildDao(),
                ) as T
            }
        }
    )
    val children = viewModel.children.collectAsState().value
    val expandedMap = viewModel.expandedMap.collectAsState().value
    val checkedMap = viewModel.checkedMap.collectAsState().value
    val isLoading = viewModel.loading.collectAsState().value
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedIdToDelete by remember { mutableStateOf<Int?>(null) }
    var selectedChildToEdit by remember { mutableStateOf<Children?>(null) }


    LaunchedEffect(Unit) {
        viewModel.fetchUsersForRoles()
        viewModel.fetchAllChildren()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color.Gray),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Filter")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }

                Button(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7367F0),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = PrimaryColor
                    )
                }
            } else {
                LazyColumn {
                    if (children.isNotEmpty()) {
                        itemsIndexed(children) { index, child ->
                            val isFirst = index == 0
                            val isLast = index == children.lastIndex

                            val shape = when {
                                isFirst && isLast -> RoundedCornerShape(16.dp)
                                isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                                else -> RectangleShape
                            }

                            AdminCardItem(
                                id = child.id.toString(),
                                title = child.fullName,
                                subtitle = "Age: ${child.age} | Parent: ${child.parent}",
                                details = listOf(
                                    "Forenames" to child.forenames,
                                    "Surnames" to child.surnames,
                                    "Birth" to child.birth,
                                    "Medical Info" to child.medicalInfo,
                                    "Driver" to child.driver,
                                ),
                                isExpanded = expandedMap[child.id.toString()] == true,
                                isChecked = checkedMap[child.id.toString()] == true,
                                shape = shape,
                                onCheckedChange = { viewModel.setChecked(child.id.toString(), it) },
                                onEditClick = {
                                    selectedChildToEdit = child.toChildren(
                                        parents = viewModel.parents,
                                        drivers = viewModel.drivers
                                    )

                                    showDialog = selectedChildToEdit != null
                                },
                                onDeleteClick = {
                                    selectedIdToDelete = child.id
                                    showDeleteDialog = true
                                },
                                onToggleExpand = { viewModel.toggleExpand(child.id.toString()) }
                            )
                        }
                    } else {
                        item {
                            Text(
                                "Click 'Add' to create a new child.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddChildDialogAdmin(
            existingChild = selectedChildToEdit,
            onDismiss = {
                showDialog = false
                selectedChildToEdit = null
            },
            onSave = {
                showDialog = false
                selectedChildToEdit = null
            }
        )
    }


    if (showDeleteDialog && selectedIdToDelete != null) {
        ConfirmationDialog(
            message = "Are you sure you want to delete this item?",
            onConfirm = {
                viewModel.deleteChild(selectedIdToDelete!!)
                showDeleteDialog = false
                selectedIdToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                selectedIdToDelete = null
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChildrenAdminScreenPreview() {
    ChildrenAdminScreen()
}