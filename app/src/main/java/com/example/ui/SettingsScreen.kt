package com.example.ui
import androidx.compose.ui.geometry.Offset

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    
    val bgGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFD4F0FF),
            Color(0xFFFFDFE9),
            Color(0xFFE5E0FF),
            Color(0xFFFFE3D5)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFF1E1E1E)
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .noiseOverlay(userProfile.uiNoiseLevel)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Voice Feedback", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp), color = Color(0xFF1E1E1E))
                
                GlassCard {
                    Column {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Enable Announcements", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E)) },
                            supportingContent = { Text("Voice feedback during workouts", color = Color.DarkGray) },
                            trailingContent = {
                                Switch(
                                    checked = userProfile.announcementsEnabled,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            viewModel.updateProfile(userProfile.copy(announcementsEnabled = checked))
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7F00FF))
                                )
                            }
                        )
                        HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Announcement Interval (Steps)", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E)) },
                            supportingContent = { Text("Current: ${userProfile.announceStepsInterval}", color = Color.DarkGray) },
                            trailingContent = {
                                var expanded by remember { mutableStateOf(false) }
                                val options = listOf(500, 1000, 2000, 5000)
                                Box {
                                    TextButton(onClick = { expanded = true }) {
                                        Text("Change", color = Color(0xFF7F00FF))
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(Color.White)
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
                        HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text("Announcement Interval (Distance)", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E)) },
                            supportingContent = { Text("Current: ${userProfile.announceDistanceIntervalKm} km", color = Color.DarkGray) },
                            trailingContent = {
                                var expanded by remember { mutableStateOf(false) }
                                val options = listOf(0.5f, 1.0f, 2.0f, 5.0f)
                                Box {
                                    TextButton(onClick = { expanded = true }) {
                                        Text("Change", color = Color(0xFF7F00FF))
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(Color.White)
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
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text("Widget Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp), color = Color(0xFF1E1E1E))
                
                GlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Widget Background Opacity", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E))
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = userProfile.widgetOpacity,
                            onValueChange = { newValue ->
                                coroutineScope.launch {
                                    viewModel.updateProfile(userProfile.copy(widgetOpacity = newValue))
                                }
                            },
                            onValueChangeFinished = {
                                coroutineScope.launch {
                                    com.example.widget.HealthWidget().updateAll(context)
                                }
                            },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF7F00FF), activeTrackColor = Color(0xFF7F00FF))
                        )
                        Text(
                            text = "${(userProfile.widgetOpacity * 100).roundToInt()}%",
                            modifier = Modifier.align(Alignment.End),
                            color = Color.DarkGray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text("UI Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp), color = Color(0xFF1E1E1E))
                
                GlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Background Noise Strength", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E))
                        Text("Adds a textured grain effect to backgrounds", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = userProfile.uiNoiseLevel,
                            onValueChange = { newValue ->
                                coroutineScope.launch {
                                    viewModel.updateProfile(userProfile.copy(uiNoiseLevel = newValue))
                                }
                            },
                            valueRange = 0f..0.2f, // Max 20% noise to prevent it from getting too intense
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF7F00FF), activeTrackColor = Color(0xFF7F00FF))
                        )
                        Text(
                            text = "${(userProfile.uiNoiseLevel * 100).roundToInt()}%",
                            modifier = Modifier.align(Alignment.End),
                            color = Color.DarkGray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
