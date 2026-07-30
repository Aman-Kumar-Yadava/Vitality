#!/bin/bash
cat << 'INNER_EOF' >> app/src/main/java/com/example/data/StepTrackerManager.kt

    fun setCustomActivity(steps: Int, distance: Float, calories: Float) {
        coroutineScope.launch {
            val today = getCurrentDateString()
            repository.updateCustomDataForDate(today, steps, distance, calories)
            _currentSteps.value = steps
            lastSavedSteps = steps
            initialStepCount = -1f
            updateWidget()
        }
    }
INNER_EOF
