#!/bin/bash
sed -i 's/import androidx.compose.material.icons.Icons/import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.rounded.ArrowForward/g' app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.kt
