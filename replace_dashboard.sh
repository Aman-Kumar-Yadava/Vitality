#!/bin/bash
sed -i 's/Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {/BoxWithConstraints(modifier = Modifier.fillMaxSize().background(bgGradient).noiseOverlay(userProfile.uiNoiseLevel)) {\n        val screenHeight = maxHeight/g' app/src/main/java/com/example/ui/DashboardScreen.kt

sed -i 's/item {/item {\n                Column(modifier = Modifier.fillMaxWidth().height(screenHeight - 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {/g' app/src/main/java/com/example/ui/DashboardScreen.kt
