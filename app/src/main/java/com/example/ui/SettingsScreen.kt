package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.White,
                            androidx.compose.ui.graphics.Color(0xFFF0F4F8)
                        )
                    )
                )
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            
            Text("Voice Feedback", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.primary)
            ListItem(
                headlineContent = { Text("Enable Announcements") },
                supportingContent = { Text("Voice feedback during workouts") },
                trailingContent = {
                    Switch(
                        checked = userProfile.announcementsEnabled,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                viewModel.updateProfile(userProfile.copy(announcementsEnabled = checked))
                            }
                        }
                    )
                }
            )
            
            ListItem(
                headlineContent = { Text("Announcement Interval (Steps)") },
                supportingContent = { Text("Current: ${userProfile.announceStepsInterval}") },
                trailingContent = {
                    var expanded by remember { mutableStateOf(false) }
                    val options = listOf(500, 1000, 2000, 5000)
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text("Change")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("$option steps") },
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.updateProfile(userProfile.copy(announceStepsInterval = option))
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
            
            ListItem(
                headlineContent = { Text("Announcement Interval (Distance)") },
                supportingContent = { Text("Current: ${userProfile.announceDistanceIntervalKm} km") },
                trailingContent = {
                    var expanded by remember { mutableStateOf(false) }
                    val options = listOf(0.5f, 1.0f, 2.0f, 5.0f)
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text("Change")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("$option km") },
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.updateProfile(userProfile.copy(announceDistanceIntervalKm = option))
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("Widget Settings", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.primary)
            ListItem(
                headlineContent = { Text("Widget Opacity") },
                supportingContent = { 
                    Column {
                        Slider(
                            value = userProfile.widgetOpacity,
                            onValueChange = { value ->
                                coroutineScope.launch {
                                    viewModel.updateProfile(userProfile.copy(widgetOpacity = value))
                                    com.example.widget.HealthWidget().updateAll(context)
                                }
                            },
                            valueRange = 0.2f..1.0f
                        )
                        Text("${(userProfile.widgetOpacity * 100).roundToInt()}%", style = MaterialTheme.typography.bodySmall)
                    }
                }
            )
        }
    }
}
