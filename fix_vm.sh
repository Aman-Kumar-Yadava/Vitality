#!/bin/bash
cat << 'INNER_EOF' >> app/src/main/java/com/example/viewmodel/MainViewModel.kt

    fun setCustomActivity(steps: Int, distance: Float, calories: Float) {
        stepTrackerManager.setCustomActivity(steps, distance, calories)
    }
INNER_EOF
