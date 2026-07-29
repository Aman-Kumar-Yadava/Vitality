#!/bin/bash
# Remove the first line of DashboardScreen.kt and MainScreen.kt if it is an import, and add it below the package statement.
sed -i '/^import androidx.compose.material.icons.automirrored.rounded.ArrowForward/d' app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i '/^import androidx.compose.foundation.border/d' app/src/main/java/com/example/ui/MainScreen.kt

sed -i 's/^package com.example.ui/package com.example.ui\n\nimport androidx.compose.material.icons.automirrored.rounded.ArrowForward/' app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i 's/^package com.example.ui/package com.example.ui\n\nimport androidx.compose.foundation.border/' app/src/main/java/com/example/ui/MainScreen.kt
