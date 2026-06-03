package com.example.workouttracker.util

/**
 * Utility object for BMI calculations.
 * Covers unit conversions (lb to kg, inches to meters) and category classifications.
 */
object BmiCalculator {
    const val LB_TO_KG = 0.453592
    const val INCH_TO_M = 0.0254

    fun calculateBmi(weight: Double, height: Double, weightUnit: String, heightUnit: String): Double {
        if (weight <= 0.0 || height <= 0.0) return 0.0
        
        val weightKg = if (weightUnit.equals("lb", ignoreCase = true)) {
            weight * LB_TO_KG
        } else {
            weight
        }

        val heightMeters = if (heightUnit.equals("inches", ignoreCase = true) || heightUnit.equals("inch", ignoreCase = true)) {
            height * INCH_TO_M
        } else {
            height
        }

        if (heightMeters <= 0.0) return 0.0
        return weightKg / (heightMeters * heightMeters)
    }

    fun getCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal Weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }
}
