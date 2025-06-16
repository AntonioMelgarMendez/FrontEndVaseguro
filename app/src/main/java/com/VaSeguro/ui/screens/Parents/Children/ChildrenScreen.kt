package com.VaSeguro.ui.screens.Parents.Children

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.VaSeguro.data.model.Child.Child
import com.VaSeguro.ui.components.AddDialogues.AddChildDialog
import com.VaSeguro.ui.components.Cards.ChildrenCard

@Composable
fun ChildrenScreen(
    viewModel: ChildrenViewModel = viewModel(factory = ChildrenViewModel.Factory)
) {
    val expandedMap = viewModel.expandedMap.collectAsState().value
    val children = viewModel.children.collectAsState().value
    val drivers = viewModel.drivers.collectAsState().value
    val parents = viewModel.parents.collectAsState().value
    var editingChild by remember { mutableStateOf<Child?>(null) }
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    if (children.isNotEmpty()) {
                        items(children) { child ->
                            ChildrenCard(
                                child = child,
                                isExpanded = expandedMap[child.id] ?: false,
                                onToggleExpand = { viewModel.toggleExpand(child.id) },
                                onEditClick = {
                                    editingChild = child
                                    showDialog = true
                                },
                                onDeleteClick = { viewModel.deleteChild(child.id) },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        item{
                            Text("No hay niños, agrega uno tocando el botón de abajo",)
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
                onConfirm = { child ->
                    if (editingChild == null) {
                        viewModel.addChild(child)
                    } else {
                        viewModel.updateChild(child)
                    }
                    showDialog = false
                    editingChild = null
                },
                drivers = drivers,
                parents = parents,
                existingChild = editingChild
            )
        }

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

@Preview(showBackground = true)
@Composable
fun ChildrenScreenPreview(){
    ChildrenScreen()
}