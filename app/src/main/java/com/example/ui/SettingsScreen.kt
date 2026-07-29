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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    
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
            
            HorizontalDivider()
            
            if (userProfile.announcementsEnabled) {
                
                Text(
                    "Announcement Intervals", 
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Steps Interval
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Steps")
                        Text(if (userProfile.announceStepsInterval > 0) "${userProfile.announceStepsInterval} steps" else "Off")
                    }
                    Slider(
                        value = userProfile.announceStepsInterval.toFloat(),
                        onValueChange = { value ->
                            coroutineScope.launch {
                                viewModel.updateProfile(userProfile.copy(announceStepsInterval = value.roundToInt()))
                            }
                        },
                        valueRange = 0f..5000f,
                        steps = 9 // 0, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000
                    )
                }
                
                // Distance Interval
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distance")
                        Text(if (userProfile.announceDistanceIntervalKm > 0f) "${String.format("%.1f", userProfile.announceDistanceIntervalKm)} km" else "Off")
                    }
                    Slider(
                        value = userProfile.announceDistanceIntervalKm,
                        onValueChange = { value ->
                            coroutineScope.launch {
                                // Round to nearest 0.5
                                val rounded = (value * 2).roundToInt() / 2f
                                viewModel.updateProfile(userProfile.copy(announceDistanceIntervalKm = rounded))
                            }
                        },
                        valueRange = 0f..5f,
                        steps = 9 // 0.5, 1.0, 1.5, ...
                    )
                }
                
                // Calories Interval
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Calories")
                        Text(if (userProfile.announceCaloriesInterval > 0f) "${userProfile.announceCaloriesInterval.toInt()} kcal" else "Off")
                    }
                    Slider(
                        value = userProfile.announceCaloriesInterval,
                        onValueChange = { value ->
                            coroutineScope.launch {
                                val rounded = (value / 50).roundToInt() * 50f
                                viewModel.updateProfile(userProfile.copy(announceCaloriesInterval = rounded))
                            }
                        },
                        valueRange = 0f..1000f,
                        steps = 19
                    )
                }
                
                HorizontalDivider()
                
                Text(
                    "Voice Settings", 
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                val context = androidx.compose.ui.platform.LocalContext.current
                var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
                
                DisposableEffect(context) {
                    val textToSpeech = android.speech.tts.TextToSpeech(context) { _ -> }
                    tts = textToSpeech
                    onDispose {
                        textToSpeech.stop()
                        textToSpeech.shutdown()
                    }
                }
                
                Button(
                    onClick = {
                        tts?.apply {
                            setSpeechRate(userProfile.speechRate)
                            setPitch(userProfile.pitch)
                            speak("This is a sample of your voice settings.", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
                ) {
                    Text("Test Voice Settings")
                }
                
                // Speech Rate
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Rate")
                        Text("${String.format("%.1f", userProfile.speechRate)}x")
                    }
                    Slider(
                        value = userProfile.speechRate,
                        onValueChange = { value ->
                            coroutineScope.launch {
                                viewModel.updateProfile(userProfile.copy(speechRate = value))
                            }
                        },
                        valueRange = 0.5f..2.0f
                    )
                }
                
                // Pitch
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pitch")
                        Text(String.format("%.1f", userProfile.pitch))
                    }
                    Slider(
                        value = userProfile.pitch,
                        onValueChange = { value ->
                            coroutineScope.launch {
                                viewModel.updateProfile(userProfile.copy(pitch = value))
                            }
                        },
                        valueRange = 0.5f..2.0f
                    )
                }
            }
        }
    }
}
