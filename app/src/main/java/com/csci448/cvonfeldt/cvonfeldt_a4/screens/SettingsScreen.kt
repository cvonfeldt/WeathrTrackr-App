package com.csci448.cvonfeldt.cvonfeldt_a4.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csci448.cvonfeldt.cvonfeldt_a4.viewmodel.IWeatherViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(  //screen for managing app settings and database operations
    modifier: Modifier = Modifier,
    weatherViewModel: IWeatherViewModel,
    coroutineScope: CoroutineScope
) {
    //Track dialog state and database save preference
    var showDeleteDialog by remember { mutableStateOf(false) }
    val saveToDatabase by weatherViewModel.saveToDatabase.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // toggle switch for database saving preference!
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Save checkpoints to database",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = saveToDatabase,
                onCheckedChange = {
                    coroutineScope.launch {
                        weatherViewModel.toggleSaveToDatabase()
                    }
                }
            )
        }

        Divider()

        Text(

            text = "Database Management",
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete All Checkpoints")
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog =false },
            title = { Text("Delete All Checkpoints") },
            text = { Text("Are you sure you want to delete all checkpoints? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            weatherViewModel.deleteAllCheckpoints()
                            showDeleteDialog = false
                        }
                    }
                ) {
                    Text(
                        text = "Delete All",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}