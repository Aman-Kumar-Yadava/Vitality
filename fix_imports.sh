#!/bin/bash
# Remove all bad imports
sed -i '/import Icons/d' app/src/main/java/com/example/ui/components/PremiumAnimatedRing.kt
sed -i '/import Icons/d' app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.kt

# Add the proper ones after package declaration
sed -i '/package com.example.ui.components/a import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.rounded.DirectionsRun\nimport androidx.compose.material.icons.automirrored.rounded.ArrowForward' app/src/main/java/com/example/ui/components/PremiumAnimatedRing.kt

sed -i '/package com.example.ui.components/a import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.rounded.DirectionsRun\nimport androidx.compose.material.icons.automirrored.rounded.ArrowForward' app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.kt
