package com.example.workouttracker.util

/**
 * Utility object for weather-based workout recommendations.
 * Maps weather condition inputs (temperature, main, description) to user suggestions.
 */
object WeatherRecommendation {
    fun getWorkoutRecommendation(temp: Double, mainWeather: String, description: String): String {
        val mainLower = mainWeather.lowercase()
        val descLower = description.lowercase()
        
        return when {
            mainLower.contains("rain") || mainLower.contains("drizzle") || mainLower.contains("thunderstorm") -> {
                "Rain detected!. Don't forget to take an umbrella with you."
            }
            mainLower.contains("fog") || mainLower.contains("smoke") || mainLower.contains("haze") || mainLower.contains("mist") || descLower.contains("smoky") || descLower.contains("foggy") -> {
                "Indoors today due to air quality / low visibility."
            }
            temp > 38.0 -> {
                "Indoor workout. Avoid the heat of ${temp.toInt()}°C. Stay hydrated!"
            }
            temp < 10.0 -> {
                "Indoor workout. Too cold (${temp.toInt()}°C) outside today."
            }
            temp in 20.0..28.0 -> {
                "Outdoor workout! Pleasant ${temp.toInt()}°C. Perfect day for outdoor cardio!"
            }
            else -> {
                "Great day for a regular workout session!"
            }
        }
    }
}
