package com.example.ui
import androidx.compose.ui.geometry.Offset

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (
        userName: String,
        stepGoal: Int,
        distGoalKm: Float,
        calGoal: Int,
        height: Float,
        weight: Float,
        age: Int,
        gender: String
    ) -> Unit
) {
    var nameStr by remember { mutableStateOf("Alex") }
    var stepGoalStr by remember { mutableStateOf("10000") }
    var distGoalStr by remember { mutableStateOf("8.0") }
    var calGoalStr by remember { mutableStateOf("500") }
    var heightStr by remember { mutableStateOf("170") }
    var weightStr by remember { mutableStateOf("70") }
    var ageStr by remember { mutableStateOf("25") }
    
    val genders = listOf("Male", "Female", "Other")
    var selectedGender by remember { mutableStateOf(genders[0]) }
    var expanded by remember { mutableStateOf(false) }
    
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
                title = { Text("Welcome to Vitality", fontWeight = FontWeight.ExtraBold) },
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
                .noiseOverlay(0f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Let's set up your profile and daily goals to personalize your tracking.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
                
                GlassCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Profile Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E1E1E)
                        )

                        OutlinedTextField(
                            value = nameStr,
                            onValueChange = { nameStr = it },
                            label = { Text("Your Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                                focusedBorderColor = Color(0xFF7F00FF),
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = heightStr,
                                onValueChange = { heightStr = it },
                                label = { Text("Height (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    focusedBorderColor = Color(0xFF7F00FF),
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                            
                            OutlinedTextField(
                                value = weightStr,
                                onValueChange = { weightStr = it },
                                label = { Text("Weight (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    focusedBorderColor = Color(0xFF7F00FF),
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = ageStr,
                                onValueChange = { ageStr = it },
                                label = { Text("Age") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    focusedBorderColor = Color(0xFF7F00FF),
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                            
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedGender,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Gender") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                                        focusedBorderColor = Color(0xFF7F00FF),
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    genders.forEach { selectionOption ->
                                        DropdownMenuItem(
                                            text = { Text(selectionOption) },
                                            onClick = {
                                                selectedGender = selectionOption
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daily Goals",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E1E1E)
                        )

                        OutlinedTextField(
                            value = stepGoalStr,
                            onValueChange = { stepGoalStr = it },
                            label = { Text("Daily Steps Goal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                                focusedBorderColor = Color(0xFF7F00FF),
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = distGoalStr,
                                onValueChange = { distGoalStr = it },
                                label = { Text("Distance Goal (km)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    focusedBorderColor = Color(0xFF7F00FF),
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )

                            OutlinedTextField(
                                value = calGoalStr,
                                onValueChange = { calGoalStr = it },
                                label = { Text("Calories Goal (kcal)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                                    focusedBorderColor = Color(0xFF7F00FF),
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val btnGradient = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF7F00FF), Color(0xFFFF007F), Color(0xFFFF8C00))
                )
                
                Button(
                    onClick = {
                        val name = nameStr.ifBlank { "User" }
                        val stepGoal = stepGoalStr.toIntOrNull() ?: 10000
                        val distGoal = distGoalStr.toFloatOrNull() ?: 8.0f
                        val calGoal = calGoalStr.toIntOrNull() ?: 500
                        val height = heightStr.toFloatOrNull() ?: 170f
                        val weight = weightStr.toFloatOrNull() ?: 70f
                        val age = ageStr.toIntOrNull() ?: 25
                        onComplete(name, stepGoal, distGoal, calGoal, height, weight, age, selectedGender)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(btnGradient, RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
