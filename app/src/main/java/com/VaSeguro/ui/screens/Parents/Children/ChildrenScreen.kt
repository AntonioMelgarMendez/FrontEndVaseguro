package com.VaSeguro.ui.screens.Parents.Children

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.VaSeguro.data.AppProvider
import com.VaSeguro.data.model.Children.Children
import com.VaSeguro.ui.components.AddDialogues.AddChildDialog
import com.VaSeguro.ui.components.Cards.ChildrenCard
import com.VaSeguro.ui.navigations.ChatScreenNavigation

@Composable
fun ChildrenScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: ChildrenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val appProvider = AppProvider(context.applicationContext)
                return ChildrenViewModel(
                    appProvider.provideChildrenRepository(),
                    appProvider.provideUserPreferences()
                ) as T
            }
        }
    )
    val expandedMap by viewModel.expandedMap.collectAsState()
    val children by viewModel.children.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var editingChild by remember { mutableStateOf<Children?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Children",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                children.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        items(children) { child ->
                            ChildrenCard(
                                child = child,
                                isExpanded = expandedMap[child.id.toString()] ?: false,
                                isDriver = !viewModel.canEdit.collectAsState().value,
                                onToggleExpand = { viewModel.toggleExpand(child.id.toString()) },
                                onEditClick = {
                                    editingChild = child
                                    showDialog = true
                                },
                                onDeleteClick = { viewModel.deleteChild(context, child.id.toString()) },
                                onChat = { navController.navigate(ChatScreenNavigation) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp, bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChildCare,
                            contentDescription = "No hay hijos",
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No se han encontrado hijos",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Agrega un hijo presionando el botón de abajo",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddChildDialog(
                onDismiss = {
                    showDialog = false
                    editingChild = null
                },
                onConfirm = { child, profileImageUri ->
                    if (editingChild == null) {
                        viewModel.addChild(
                            context = context,
                            forenames = child.forenames,
                            surnames = child.surnames,
                            birthDate = child.birth_date,
                            medicalInfo = child.medical_info,
                            gender = child.gender,
                            profileImageUri = profileImageUri,
                            onSuccess = {
                                showDialog = false
                                editingChild = null
                            },
                            onError = {
                                // Show error message if needed
                            }
                        )
                    } else {
                        viewModel.updateChild(
                            context = context,
                            updatedChild = child,
                            profileImageUri = profileImageUri,
                            onSuccess = {
                                showDialog = false
                                editingChild = null
                            },
                            onError = {
                                // Show error message if needed
                            }
                        )
                    }
                },
                existingChild = editingChild
            )
        }

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }

        if (viewModel.canEdit.collectAsState().value) {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add child"
                )
            }
        }
    }
}