package com.example.data

import java.util.Locale
import kotlin.math.roundToInt

object FitnessCalculations {

    /**
     * Base Stride length in meters based on height and gender.
     * Male: Height (m) * 0.415
     * Female: Height (m) * 0.413
     */
    fun calculateBaseStrideMeters(heightCm: Float, gender: String): Float {
        val isFemale = gender.equals("Female", ignoreCase = true)
        val factor = if (isFemale) 0.00413f else 0.00415f
        return (heightCm * factor).coerceAtLeast(0.40f)
    }

    /**
     * Stride multiplier based on cadence (steps/min).
     * Slow (<80): 0.95
     * Normal (80-110): 1.00
     * Fast (110-130): 1.05
     * Very fast (>130): 1.10
     */
    fun getCadenceMultiplier(cadenceStepsPerMin: Float): Float {
        return when {
            cadenceStepsPerMin <= 0f -> 1.00f
            cadenceStepsPerMin < 80f -> 0.95f
            cadenceStepsPerMin <= 110f -> 1.00f
            cadenceStepsPerMin <= 130f -> 1.05f
            else -> 1.10f
        }
    }

    /**
     * Adaptive stride length in meters calculated dynamically using height, gender, and cadence.
     */
    fun calculateAdaptiveStrideMeters(heightCm: Float, gender: String, cadenceStepsPerMin: Float): Float {
        val base = calculateBaseStrideMeters(heightCm, gender)
        val mult = getCadenceMultiplier(cadenceStepsPerMin)
        return base * mult
    }

    /**
     * Calculate Distance in km using steps, adaptive stride length.
     */
    fun calculateDistanceKm(
        steps: Int,
        heightCm: Float,
        gender: String,
        cadenceStepsPerMin: Float = 100f
    ): Float {
        if (steps <= 0) return 0f
        val strideMeters = calculateAdaptiveStrideMeters(heightCm, gender, cadenceStepsPerMin)
        return (steps * strideMeters) / 1000f
    }

    /**
     * Calculate active duration in minutes from steps and average cadence or session duration.
     */
    fun calculateActiveDurationMinutes(steps: Int, cadenceStepsPerMin: Float = 100f): Float {
        if (steps <= 0) return 0f
        val validCadence = if (cadenceStepsPerMin > 10f) cadenceStepsPerMin else 100f
        return steps / validCadence
    }

    /**
     * Calculate walking speed in km/h.
     */
    fun calculateSpeedKmh(distanceKm: Float, durationHours: Float): Float {
        if (distanceKm <= 0f || durationHours <= 0f) return 0f
        return distanceKm / durationHours
    }

    /**
     * Calculate pace in seconds per km.
     */
    fun calculatePaceSecondsPerKm(distanceKm: Float, durationMinutes: Float): Int {
        if (distanceKm <= 0.001f || durationMinutes <= 0f) return 0
        val paceMinPerKm = durationMinutes / distanceKm
        return (paceMinPerKm * 60f).roundToInt()
    }

    /**
     * Format pace in mm:ss (e.g. "12:05"). If zero or invalid distance, returns "--:--".
     */
    fun formatPace(paceSecondsPerKm: Int): String {
        if (paceSecondsPerKm <= 0 || paceSecondsPerKm > 7200) return "--:--"
        val minutes = paceSecondsPerKm / 60
        val seconds = paceSecondsPerKm % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    /**
     * Format pace with unit (e.g. "12:05 min/km" or "--:--").
     */
    fun formatPaceWithUnit(paceSecondsPerKm: Int): String {
        val formatted = formatPace(paceSecondsPerKm)
        return if (formatted == "--:--") "--:--" else "$formatted min/km"
    }

    /**
     * Select MET value automatically based on speed (km/h) or pace (min/km).
     * Very slow (<3.2 km/h): 2.0 MET
     * Slow (3.2 - 4.0 km/h): 2.8 MET
     * Normal (4.0 - 5.2 km/h): 3.5 MET
     * Brisk (5.2 - 6.0 km/h): 4.3 MET
     * Fast (6.0 - 7.2 km/h): 5.0 MET
     * Very fast (>7.2 km/h): 6.3 MET
     */
    fun determineMetFromSpeed(speedKmh: Float): Float {
        return when {
            speedKmh <= 0f -> 3.5f
            speedKmh < 3.2f -> 2.0f
            speedKmh < 4.0f -> 2.8f
            speedKmh < 5.2f -> 3.5f
            speedKmh < 6.0f -> 4.3f
            speedKmh < 7.2f -> 5.0f
            else -> 6.3f
        }
    }

    /**
     * Select MET based on pace seconds per km.
     */
    fun determineMetFromPaceSeconds(paceSecondsPerKm: Int, fallbackSpeedKmh: Float = 0f): Float {
        if (paceSecondsPerKm <= 0) return determineMetFromSpeed(fallbackSpeedKmh)
        val paceMinKm = paceSecondsPerKm / 60f
        return when {
            paceMinKm > 18.75f -> 2.0f // < 3.2 km/h
            paceMinKm > 15.00f -> 2.8f // 3.2 - 4.0 km/h
            paceMinKm > 11.54f -> 3.5f // 4.0 - 5.2 km/h
            paceMinKm > 10.00f -> 4.3f // 5.2 - 6.0 km/h
            paceMinKm > 8.33f -> 5.0f  // 6.0 - 7.2 km/h
            else -> 6.3f                // > 7.2 km/h
        }
    }

    /**
     * Calculate Active Calories burned = MET * Weight (kg) * Duration (hours)
     */
    fun calculateActiveCalories(met: Float, weightKg: Float, durationHours: Float): Float {
        if (weightKg <= 0f || durationHours <= 0f) return 0f
        return (met * weightKg * durationHours).coerceAtLeast(0f)
    }

    /**
     * Calculate Estimated Total Calories = Active Calories + BMR during duration.
     * BMR factor ~ 1.0 kcal / kg / hour.
     */
    fun calculateTotalCalories(activeCalories: Float, weightKg: Float, durationHours: Float): Float {
        if (durationHours <= 0f) return activeCalories
        val bmrCalories = 1.0f * weightKg * durationHours
        return activeCalories + bmrCalories
    }
}
