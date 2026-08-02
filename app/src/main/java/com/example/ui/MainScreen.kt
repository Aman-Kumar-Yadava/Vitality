package com.example.ui

import androidx.compose.foundation.border

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.viewmodel.MainViewModel

sealed class Screen(
    val route: String, 
    val title: String, 
    val icon: ImageVector
) {
    object Dashboard : Screen("dashboard", "Today", Icons.Rounded.Home)
    object History : Screen("history", "Trends", Icons.Rounded.ShowChart)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object DistanceDetails : Screen("distance_details", "Distance", Icons.Filled.DirectionsWalk)
    object CaloriesDetails : Screen("calories_details", "Calories", Icons.Rounded.LocalFireDepartment)
    object PaceDetails : Screen("pace_details", "Pace", Icons.Rounded.ShowChart)
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isSettingsOpen = currentDestination?.route == Screen.Settings.route
    val isDistanceDetailsOpen = currentDestination?.route == Screen.DistanceDetails.route
    val isCaloriesDetailsOpen = currentDestination?.route == Screen.CaloriesDetails.route
    val isPaceDetailsOpen = currentDestination?.route == Screen.PaceDetails.route
    val hideNavAndSettings = isSettingsOpen || isDistanceDetailsOpen || isCaloriesDetailsOpen || isPaceDetailsOpen
    val items = listOf(Screen.Dashboard, Screen.History)
    
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    viewModel = viewModel,
                    onDistanceClick = { navController.navigate(Screen.DistanceDetails.route) },
                    onCaloriesClick = { navController.navigate(Screen.CaloriesDetails.route) },
                    onPaceClick = { navController.navigate(Screen.PaceDetails.route) }
                ) 
            }
            composable(Screen.History.route) { HistoryScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
            composable(Screen.DistanceDetails.route) { 
                DistanceDetailsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onPaceClick = { navController.navigate(Screen.PaceDetails.route) }
                ) 
            }
            composable(Screen.CaloriesDetails.route) { 
                CaloriesDetailsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                ) 
            }
            composable(Screen.PaceDetails.route) {
                PaceDetailsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        
        // Floating Bottom Navigation Bar
        if (!hideNavAndSettings) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .wrapContentWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .noiseOverlay(userProfile.pillMenuNoiseLevel)
                    .border(1.dp, Color.White, RoundedCornerShape(26.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        val color = if (selected) Color(0xFF7F00FF) else Color.Gray
                        
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = color,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = color,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
        
        // Settings Icon in Top Right (hidden on Distance & Calories Details screens)
        if (!isDistanceDetailsOpen && !isCaloriesDetailsOpen) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.5f), CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            if (isSettingsOpen) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(Screen.Settings.route)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSettingsOpen) Icons.Rounded.Close else Icons.Filled.Settings,
                        contentDescription = if (isSettingsOpen) "Close Settings" else "Settings",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
