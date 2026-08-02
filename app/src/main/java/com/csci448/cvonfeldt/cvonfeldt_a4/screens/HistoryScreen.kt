package com.csci448.cvonfeldt.cvonfeldt_a4.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.csci448.cvonfeldt.cvonfeldt_a4.viewmodel.IWeatherViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    weatherViewModel: IWeatherViewModel,
    coroutineScope: CoroutineScope

) {
    val checkpoints by weatherViewModel.checkpoints.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    val dismissedItems = remember { mutableStateListOf<Long>() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement =Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = checkpoints,
            key = { checkpoint -> checkpoint.id }
        ) { checkpoint ->
            if (!dismissedItems.contains(checkpoint.id)) {
                var isDeleted by remember { mutableStateOf(false) }

                if (!isDeleted) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        //delete background
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(MaterialTheme.colorScheme.error)
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Delete",
                                    color = MaterialTheme.colorScheme.onError,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onError
                                )
                            }
                        }

                        // Card content
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .swipeToDismiss(
                                    onDismiss = {
                                        coroutineScope.launch {
                                            weatherViewModel.deleteCheckpoint(checkpoint)
                                            dismissedItems.add(checkpoint.id)
                                            isDeleted = true
                                        }
                                    }
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = checkpoint.address,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                weatherViewModel.updateCheckpoint(
                                                    checkpoint.copy(isLocked = !checkpoint.isLocked)
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            if (checkpoint.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = if (checkpoint.isLocked) "Unlock" else "Lock"
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Temperature: ${checkpoint.temperature}°F",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Weather: ${checkpoint.weatherDescription}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Checked in: ${dateFormat.format(checkpoint.timestamp)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun Modifier.swipeToDismiss(
    onDismiss: () -> Unit
): Modifier = composed {
    val offsetX = remember { mutableStateOf(0f) }

    this
        .offset { IntOffset(offsetX.value.toInt(), 0) }
        .pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                offsetX.value = (offsetX.value + dragAmount).coerceIn(-size.width.toFloat(), 0f)

                if (offsetX.value <= -size.width / 2) {
                    onDismiss()
                }
            }
        }
}