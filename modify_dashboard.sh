#!/bin/bash
sed -i 's/import androidx.compose.ui.graphics.vector.ImageVector/import androidx.compose.ui.graphics.vector.ImageVector\nimport com.example.ui.components.PremiumAnimatedRing\nimport com.example.ui.components.PremiumAnimatedWaveCard\nimport com.example.ui.components.PulsingRunningIcon/' app/src/main/java/com/example/ui/DashboardScreen.kt
