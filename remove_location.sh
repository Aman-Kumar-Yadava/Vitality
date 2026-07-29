#!/bin/bash
sed -i '/\/\/ Location Chip/,/^\s*$/d' app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i '/val currentLocation = userProfile.location/d' app/src/main/java/com/example/ui/DashboardScreen.kt
