#!/bin/bash
# Remove any bad imports and add the right ones for ArrowForward and DirectionsRun
sed -i 's/androidx.compose.material.icons.automirrored.rounded.ArrowForward/Icons.AutoMirrored.Rounded.ArrowForward/g' app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.kt
sed -i 's/import androidx.compose.material.icons.Icons/import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.rounded.ArrowForward/g' app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.kt

sed -i 's/androidx.compose.material.icons.automirrored.rounded.DirectionsRun/Icons.AutoMirrored.Rounded.DirectionsRun/g' app/src/main/java/com/example/ui/components/PremiumAnimatedRing.kt
