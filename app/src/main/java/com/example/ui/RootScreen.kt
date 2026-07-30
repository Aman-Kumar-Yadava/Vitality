package com.example.ui
import androidx.compose.ui.geometry.Offset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun RootScreen(viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    var showSplash by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(2000) // Show splash for 2 seconds
        showSplash = false
    }

    AnimatedVisibility(
        visible = showSplash,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        SplashScreen()
    }

    AnimatedVisibility(
        visible = !showSplash,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        if (!userProfile.hasCompletedOnboarding) {
            OnboardingScreen(
                onComplete = { userName, stepGoal, distGoalKm, calGoal, height, weight, age, gender ->
                    viewModel.completeOnboarding(userName, stepGoal, distGoalKm, calGoal, height, weight, age, gender)
                }
            )
        } else {
            MainScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun SplashScreen() {
    var scale by remember { mutableStateOf(0.5f) }
    
    LaunchedEffect(Unit) {
        androidx.compose.animation.core.animate(
            initialValue = 0.5f,
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        ) { value, _ ->
            scale = value
        }
    }
    
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(150.dp)
                .scale(scale)
        )
    }
}
