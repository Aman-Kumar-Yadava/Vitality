package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showProfileSubScreen by remember { mutableStateOf(false) }

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
                title = {
                    Text(
                        if (showProfileSubScreen) "Profile & Goals" else "Settings",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    if (showProfileSubScreen) {
                        IconButton(onClick = { showProfileSubScreen = false }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back to Settings",
                                tint = Color(0xFF1E1E1E)
                            )
                        }
                    }
                },
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
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                if (showProfileSubScreen) {
                    // Sub-screen for Profile & Goals Detail
                    ProfileAndGoalsDetailScreen(
                        userProfile = userProfile,
                        onUpdateProfile = { updated ->
                            coroutineScope.launch {
                                viewModel.updateProfile(updated)
                            }
                        }
                    )
                } else {
                    // Main Settings Page
                    // Profile Entry Card at Top with vibrant gradient and scale press animation
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { showProfileSubScreen = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFF85A1), // Light Pink
                                            Color(0xFFC38FFF)  // Light Purple
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = "Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Profile & Activity Goals",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "${userProfile.userName} • ${userProfile.dailyStepGoal} steps • ${userProfile.dailyDistanceGoalKm} km",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowRight,
                                    contentDescription = "View Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Voice Feedback, Widget & UI Preferences Section
                    AppPreferencesSection(
                        userProfile = userProfile,
                        onUpdateProfile = { updated ->
                            coroutineScope.launch {
                                viewModel.updateProfile(updated)
                            }
                        },
                        onWidgetOpacityFinished = {
                            coroutineScope.launch {
                                try {
                                    com.example.widget.HealthWidget().updateAll(context)
                                    com.example.widget.TransparentHealthWidget().updateAll(context)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun ProfileAndGoalsDetailScreen(
    userProfile: com.example.data.UserProfile,
    onUpdateProfile: (com.example.data.UserProfile) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    // Edit form local state
    var userNameInput by remember(userProfile.userName) { mutableStateOf(userProfile.userName) }
    var heightInput by remember(userProfile.heightCm) { mutableStateOf(userProfile.heightCm.roundToInt().toString()) }
    var weightInput by remember(userProfile.weightKg) { mutableStateOf(userProfile.weightKg.roundToInt().toString()) }
    var ageInput by remember(userProfile.age) { mutableStateOf(userProfile.age.toString()) }
    var selectedGender by remember(userProfile.gender) { mutableStateOf(userProfile.gender) }

    var editStepGoal by remember(userProfile.dailyStepGoal) { mutableFloatStateOf(userProfile.dailyStepGoal.toFloat()) }
    var editDistGoal by remember(userProfile.dailyDistanceGoalKm) { mutableFloatStateOf(userProfile.dailyDistanceGoalKm) }
    var editCalGoal by remember(userProfile.dailyCaloriesGoal) { mutableFloatStateOf(userProfile.dailyCaloriesGoal.toFloat()) }

    if (!isEditing) {
        // ================= READ-ONLY PROFILE DISPLAY =================
        GlassCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFFF85A1), Color(0xFFC38FFF))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = userProfile.userName.ifBlank { "User Profile" },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E1E1E)
                        )
                        Text(
                            text = "${userProfile.gender} • ${userProfile.age} yrs old",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Body Parameters",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp),
            color = Color(0xFF1E1E1E)
        )

        GlassCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileMetricDisplay(
                        icon = Icons.Rounded.Height,
                        label = "Height",
                        value = "${userProfile.heightCm.roundToInt()} cm",
                        color = Color(0xFF7F00FF),
                        modifier = Modifier.weight(1f)
                    )
                    ProfileMetricDisplay(
                        icon = Icons.Rounded.Scale,
                        label = "Weight",
                        value = "${userProfile.weightKg.roundToInt()} kg",
                        color = Color(0xFF00B0FF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileMetricDisplay(
                        icon = Icons.Rounded.Cake,
                        label = "Age",
                        value = "${userProfile.age} years",
                        color = Color(0xFFFF8008),
                        modifier = Modifier.weight(1f)
                    )
                    ProfileMetricDisplay(
                        icon = Icons.Rounded.Wc,
                        label = "Gender",
                        value = userProfile.gender,
                        color = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Daily Activity Targets",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp),
            color = Color(0xFF1E1E1E)
        )

        GlassCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoalTargetDisplay(
                    icon = Icons.Rounded.DirectionsRun,
                    label = "Steps Target",
                    value = "${userProfile.dailyStepGoal} steps / day",
                    color = Color(0xFF7F00FF)
                )

                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                GoalTargetDisplay(
                    icon = Icons.Rounded.LocationOn,
                    label = "Distance Target",
                    value = String.format("%.1f km / day", userProfile.dailyDistanceGoalKm),
                    color = Color(0xFF00B0FF)
                )

                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

                GoalTargetDisplay(
                    icon = Icons.Rounded.LocalFireDepartment,
                    label = "Calories Target",
                    value = "${userProfile.dailyCaloriesGoal} kcal / day",
                    color = Color(0xFFFF3D00)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { isEditing = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF85A1)),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile & Goals", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

    } else {
        // ================= EDITABLE PROFILE FORM =================
        Text(
            text = "Edit Profile & Body Parameters",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp),
            color = Color(0xFF1E1E1E)
        )

        GlassCard {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = userNameInput,
                    onValueChange = { userNameInput = it },
                    label = { Text("Name") },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF7F00FF)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Height & Weight Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Height (cm)") },
                        leadingIcon = { Icon(Icons.Rounded.Height, contentDescription = null, tint = Color(0xFF7F00FF)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        leadingIcon = { Icon(Icons.Rounded.Scale, contentDescription = null, tint = Color(0xFF7F00FF)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Age Row
                OutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    label = { Text("Age (yrs)") },
                    leadingIcon = { Icon(Icons.Rounded.Cake, contentDescription = null, tint = Color(0xFF7F00FF)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gender Selector
                Text(
                    text = "Gender",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E1E1E),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Male", "Female", "Other").forEach { g ->
                        val isSelected = selectedGender.equals(g, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGender = g },
                            label = { Text(g, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7F00FF),
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Edit Daily Activity Targets",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp),
            color = Color(0xFF1E1E1E)
        )

        GlassCard {
            Column(modifier = Modifier.padding(16.dp)) {
                // Step Target Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DirectionsRun, contentDescription = null, tint = Color(0xFF7F00FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daily Steps Target", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E))
                    }
                    Text(
                        text = "${((editStepGoal / 500).roundToInt() * 500)} steps",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7F00FF)
                    )
                }
                Slider(
                    value = editStepGoal,
                    onValueChange = { editStepGoal = it },
                    valueRange = 2000f..30000f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF7F00FF), activeTrackColor = Color(0xFF7F00FF))
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(16.dp))

                // Distance Target Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color(0xFF00B0FF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daily Distance Target", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E))
                    }
                    Text(
                        text = String.format("%.1f km", (editDistGoal * 2).roundToInt() / 2.0f),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00B0FF)
                    )
                }
                Slider(
                    value = editDistGoal,
                    onValueChange = { editDistGoal = it },
                    valueRange = 1.0f..25.0f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00B0FF), activeTrackColor = Color(0xFF00B0FF))
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(16.dp))

                // Calories Target Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF3D00))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daily Calories Target", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E))
                    }
                    Text(
                        text = "${((editCalGoal / 25).roundToInt() * 25)} kcal",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF3D00)
                    )
                }
                Slider(
                    value = editCalGoal,
                    onValueChange = { editCalGoal = it },
                    valueRange = 100f..2000f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFF3D00), activeTrackColor = Color(0xFFFF3D00))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save & Cancel Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { isEditing = false },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    val parsedHeight = heightInput.toFloatOrNull() ?: userProfile.heightCm
                    val parsedWeight = weightInput.toFloatOrNull() ?: userProfile.weightKg
                    val parsedAge = ageInput.toIntOrNull() ?: userProfile.age

                    val finalStepGoal = (editStepGoal / 500).roundToInt() * 500
                    val finalDistGoal = (editDistGoal * 2).roundToInt() / 2.0f
                    val finalCalGoal = (editCalGoal / 25).roundToInt() * 25

                    onUpdateProfile(
                        userProfile.copy(
                            userName = userNameInput.ifBlank { "User" },
                            heightCm = parsedHeight,
                            weightKg = parsedWeight,
                            age = parsedAge,
                            gender = selectedGender,
                            dailyStepGoal = finalStepGoal,
                            dailyDistanceGoalKm = finalDistGoal,
                            dailyCaloriesGoal = finalCalGoal
                        )
                    )
                    isEditing = false
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F00FF)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileMetricDisplay(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.5f))
            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                Text(value, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E), fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun GoalTargetDisplay(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E), fontSize = 15.sp)
        }
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 15.sp)
    }
}

@Composable
private fun AppPreferencesSection(
    userProfile: com.example.data.UserProfile,
    onUpdateProfile: (com.example.data.UserProfile) -> Unit,
    onWidgetOpacityFinished: () -> Unit
) {
    Text(
        text = "Home Screen Progress Bar Target",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 16.dp),
        color = Color(0xFF1E1E1E)
    )

    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Primary Metric for Progress Ring",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E1E1E)
            )
            Text(
                text = "Choose which goal is displayed on the home screen main progress ring.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Steps", "Distance", "Calories").forEach { metric ->
                    val isSelected = userProfile.primaryProgressMetric.equals(metric, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFF7F00FF) else Color.White.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF7F00FF) else Color.White.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onUpdateProfile(userProfile.copy(primaryProgressMetric = metric))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = metric,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF1E1E1E),
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Text(
        text = "Voice Feedback",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 16.dp),
        color = Color(0xFF1E1E1E)
    )

    GlassCard {
        Column {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text("Enable Voice Announcements", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E)) },
                supportingContent = { Text("Voice updates during active walking sessions", color = Color.DarkGray) },
                trailingContent = {
                    Switch(
                        checked = userProfile.announcementsEnabled,
                        onCheckedChange = { checked ->
                            onUpdateProfile(userProfile.copy(announcementsEnabled = checked))
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF7F00FF))
                    )
                }
            )
            HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text("Announcement Interval (Steps)", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E)) },
                supportingContent = { Text("Current: ${userProfile.announceStepsInterval} steps", color = Color.DarkGray) },
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
                                        onUpdateProfile(userProfile.copy(announceStepsInterval = option))
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
                                        onUpdateProfile(userProfile.copy(announceDistanceIntervalKm = option))
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
                headlineContent = { Text("Announcement Interval (Calories)", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E)) },
                supportingContent = { Text("Current: ${userProfile.announceCaloriesInterval.toInt()} kcal", color = Color.DarkGray) },
                trailingContent = {
                    var expanded by remember { mutableStateOf(false) }
                    val options = listOf(50f, 100f, 200f, 500f)
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
                                    text = { Text("${option.toInt()} kcal") },
                                    onClick = {
                                        onUpdateProfile(userProfile.copy(announceCaloriesInterval = option))
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

    Text("UI Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp), color = Color(0xFF1E1E1E))

    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Background Noise Strength", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E))
            Text("Adds a textured grain effect to backgrounds", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = userProfile.uiNoiseLevel,
                onValueChange = { newValue ->
                    onUpdateProfile(userProfile.copy(uiNoiseLevel = newValue))
                },
                valueRange = 0f..0.2f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF7F00FF), activeTrackColor = Color(0xFF7F00FF))
            )
            Text(
                text = "${(userProfile.uiNoiseLevel * 100).roundToInt()}%",
                modifier = Modifier.align(Alignment.End),
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Pill Menu Noise Strength", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E))
            Text("Adds a textured grain effect to the bottom menu", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = userProfile.pillMenuNoiseLevel,
                onValueChange = { newValue ->
                    onUpdateProfile(userProfile.copy(pillMenuNoiseLevel = newValue))
                },
                valueRange = 0f..0.2f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF7F00FF), activeTrackColor = Color(0xFF7F00FF))
            )
            Text(
                text = "${(userProfile.pillMenuNoiseLevel * 100).roundToInt()}%",
                modifier = Modifier.align(Alignment.End),
                color = Color.DarkGray
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Text("Widget Settings & Options", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp), color = Color(0xFF1E1E1E))

    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Available Home Screen Widgets",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E1E1E)
            )
            Text(
                text = "Long-press your home screen to add either widget style",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Widget Previews Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Style 1: Gradient Theme Widget
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF7000FF), Color(0xFFFF007F), Color(0xFFFF8947))
                            )
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text("Gradient Widget", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.DirectionsWalk, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("8,450", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("5.8 km  •  320 kcal", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
                    }
                }

                // Style 2: Transparent Glowing White Widget
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            // Simulated dark wallpaper background to show off transparent glowing white blending
                            Brush.verticalGradient(
                                listOf(Color(0xFF111827), Color(0xFF1F2937))
                            )
                        )
                        .padding(12.dp)
                ) {
                    // Transparent PNG layer overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = userProfile.widgetOpacity * 0.2f))
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Transparent PNG", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Rounded.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.DirectionsWalk, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("8,450", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("5.8 km  •  320 kcal", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Widget Background Glass Opacity", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E1E))
            Text("Adjust background translucency (0% = completely clear PNG transparent)", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = userProfile.widgetOpacity,
                onValueChange = { newValue ->
                    onUpdateProfile(userProfile.copy(widgetOpacity = newValue))
                },
                onValueChangeFinished = onWidgetOpacityFinished,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFFFF3D00), activeTrackColor = Color(0xFFFF3D00))
            )
            Text(
                text = if (userProfile.widgetOpacity == 0f) "0% (Fully Transparent PNG)" else "${(userProfile.widgetOpacity * 100).roundToInt()}%",
                modifier = Modifier.align(Alignment.End),
                color = Color.DarkGray,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
