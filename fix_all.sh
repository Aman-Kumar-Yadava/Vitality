#!/bin/bash
cat << 'INNER_EOF' > app/src/main/java/com/example/ui/components/PremiumAnimatedRing.kt
package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
INNER_EOF
tail -n +31 app/src/main/java/com/example/ui/components/PremiumAnimatedRing.kt >> app/src/main/java/com/example/ui/components/PremiumAnimatedRing.tmp
mv app/src/main/java/com/example/ui/components/PremiumAnimatedRing.tmp app/src/main/java/com/example/ui/components/PremiumAnimatedRing.kt

cat << 'INNER_EOF2' > app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.kt
package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.noiseOverlay
import kotlin.math.sin
INNER_EOF2
# The wave card imports ended at line 34 earlier, let's find the first @Composable
tail -n +$(grep -n "@Composable" app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.kt | head -1 | cut -d: -f1) app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.kt >> app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.tmp
mv app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.tmp app/src/main/java/com/example/ui/components/PremiumAnimatedWaveCard.kt
