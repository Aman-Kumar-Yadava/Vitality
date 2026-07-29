#!/bin/bash
sed -i 's/val history by viewModel.historyRecords/val history by viewModel.allRecords/' app/src/main/java/com/example/ui/HistoryScreen.kt
sed -i '1i import androidx.compose.foundation.border' app/src/main/java/com/example/ui/MainScreen.kt
sed -i 's/androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowForward/androidx.compose.material.icons.automirrored.rounded.ArrowForward/' app/src/main/java/com/example/ui/DashboardScreen.kt
