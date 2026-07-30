#!/bin/bash
sed -i 's/val UI_NOISE_LEVEL = floatPreferencesKey("ui_noise_level")/val UI_NOISE_LEVEL = floatPreferencesKey("ui_noise_level")\n        val PILL_MENU_NOISE_LEVEL = floatPreferencesKey("pill_menu_noise_level")/' app/src/main/java/com/example/data/UserPreferencesRepository.kt

sed -i 's/uiNoiseLevel = preferences\[PreferencesKeys.UI_NOISE_LEVEL\] ?: 0.05f/uiNoiseLevel = preferences[PreferencesKeys.UI_NOISE_LEVEL] ?: 0.05f,\n            pillMenuNoiseLevel = preferences[PreferencesKeys.PILL_MENU_NOISE_LEVEL] ?: 0.05f/' app/src/main/java/com/example/data/UserPreferencesRepository.kt

sed -i 's/preferences\[PreferencesKeys.UI_NOISE_LEVEL\] = profile.uiNoiseLevel/preferences[PreferencesKeys.UI_NOISE_LEVEL] = profile.uiNoiseLevel\n            preferences[PreferencesKeys.PILL_MENU_NOISE_LEVEL] = profile.pillMenuNoiseLevel/' app/src/main/java/com/example/data/UserPreferencesRepository.kt
