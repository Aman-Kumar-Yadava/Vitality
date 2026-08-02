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
     */
    fun getCadenceMultiplier(cadenceStepsPerMin: Float): Float {
        if (cadenceStepsPerMin <= 0f) return 1.00f
        
        // Smoothly scale the multiplier based on cadence.
        // Assuming a baseline cadence of 100 spm = 1.0 multiplier.
        // For every 1 step per minute increase, stride increases by a fraction.
        val baseCadence = 100f
        val adjustmentPerStep = 0.0015f // 0.15% stride increase per spm
        
        val multiplier = 1.0f + ((cadenceStepsPerMin - baseCadence) * adjustmentPerStep)
        
        // Clamp the limits to prevent extreme edge cases
        return multiplier.coerceIn(0.85f, 1.20f)
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
     * Select MET value dynamically based on walking/running speed (km/h).
     * Uses continuous dynamic ACSM equations for precise MET recalculation:
     * - Rest / Minimal movement (<1.0 km/h): 1.2 - 2.0 METs
     * - Slow Walk (1.0 - 3.2 km/h): 2.0 - 2.8 METs
     * - Moderate Walk (3.2 - 5.0 km/h): 2.8 - 3.8 METs
     * - Brisk Walk (5.0 - 6.5 km/h): 3.8 - 5.0 METs
     * - Fast / Power Walk (6.5 - 8.0 km/h): 5.0 - 7.5 METs
     * - Jogging / Running (>8.0 km/h): 7.5 - 12.0+ METs
     */
    fun determineMetFromSpeed(speedKmh: Float): Float {
        if (speedKmh <= 0f) return 3.5f // Default moderate walking baseline MET
        return when {
            speedKmh < 1.0f -> 1.2f + (speedKmh / 1.0f) * 0.8f
            speedKmh < 3.2f -> 2.0f + ((speedKmh - 1.0f) / 2.2f) * 0.8f
            speedKmh < 5.0f -> 2.8f + ((speedKmh - 3.2f) / 1.8f) * 1.0f
            speedKmh < 6.5f -> 3.8f + ((speedKmh - 5.0f) / 1.5f) * 1.2f
            speedKmh < 8.0f -> 5.0f + ((speedKmh - 6.5f) / 1.5f) * 2.5f
            speedKmh < 12.0f -> 7.5f + ((speedKmh - 8.0f) / 4.0f) * 4.0f
            else -> (11.5f + (speedKmh - 12.0f) * 0.8f).coerceAtMost(16.0f)
        }
    }

    /**
     * Calculate MET dynamically from cadence (steps per minute).
     */
    fun determineMetFromCadence(
        cadenceStepsPerMin: Float,
        heightCm: Float = 170f,
        gender: String = "Male"
    ): Float {
        if (cadenceStepsPerMin <= 0f) return 3.5f
        val strideMeters = calculateAdaptiveStrideMeters(heightCm, gender, cadenceStepsPerMin)
        val speedKmh = (cadenceStepsPerMin * strideMeters * 60f) / 1000f
        return determineMetFromSpeed(speedKmh)
    }

    /**
     * Select MET dynamically based on pace in seconds per km or fallback speed.
     */
    fun determineMetFromPaceSeconds(paceSecondsPerKm: Int, fallbackSpeedKmh: Float = 0f): Float {
        if (paceSecondsPerKm <= 0) {
            return determineMetFromSpeed(fallbackSpeedKmh)
        }
        val speedKmh = 3600f / paceSecondsPerKm.toFloat()
        return determineMetFromSpeed(speedKmh)
    }

    /**
     * Calculate Active Calories burned = (MET - 1.0) * Weight (kg) * Duration (hours)
     */
    fun calculateActiveCalories(met: Float, weightKg: Float, durationHours: Float): Float {
        if (weightKg <= 0f || durationHours <= 0f) return 0f
        return ((met - 1.0f) * weightKg * durationHours).coerceAtLeast(0f)
    }

    /**
     * Calculate Estimated Total Calories = MET * Weight (kg) * Duration (hours)
     */
    fun calculateTotalCalories(activeCalories: Float, weightKg: Float, durationHours: Float): Float {
        if (durationHours <= 0f) return activeCalories
        // Instead of adding BMR on top, Total (Gross) Calories is just activeCalories + BMR during duration.
        // Wait, since activeCalories is now (MET-1.0)*W*H, adding BMR (1.0*W*H) gets us back to MET*W*H.
        val bmrCalories = 1.0f * weightKg * durationHours
        return activeCalories + bmrCalories
    }
}
