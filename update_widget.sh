#!/bin/bash
sed -i 's/ColorProvider(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))/ColorProvider(day = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f), night = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))/g' app/src/main/java/com/example/widget/HealthWidget.kt
sed -i 's/ColorProvider(androidx.compose.ui.graphics.Color.White)/ColorProvider(day = androidx.compose.ui.graphics.Color.White, night = androidx.compose.ui.graphics.Color.White)/g' app/src/main/java/com/example/widget/HealthWidget.kt
