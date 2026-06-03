package com.example.workouttracker

import com.example.workouttracker.util.WeatherRecommendation
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WeatherRecommendationTest
 * Covers unit tests for condition to suggestion mapping:
 * - Pleasant weather (20–28°C) mapped to outdoor cardio recommendation.
 * - Extreme heat (>38°C) mapped to indoor/hydration recommendation.
 * - Rain/drizzle/thunderstorm mapped to umbrella warning recommendation.
 * - Cold (<10°C) mapped to indoor/too cold recommendation.
 * - Fog/smoke/haze/mist/smoky/foggy mapped to air quality/visibility warning recommendation.
 */
class WeatherRecommendationTest {

    @Test
    fun testPleasantWeather() {
        val recommendation = WeatherRecommendation.getWorkoutRecommendation(24.5, "Clear", "sunny day")
        assertTrue(recommendation.contains("Outdoor workout! Pleasant"))
        assertTrue(recommendation.contains("Perfect day for outdoor cardio!"))
    }

    @Test
    fun testExtremeHeat() {
        val recommendation = WeatherRecommendation.getWorkoutRecommendation(39.0, "Clear", "extremely hot")
        assertTrue(recommendation.contains("Indoor workout"))
        assertTrue(recommendation.contains("Avoid the heat of"))
        assertTrue(recommendation.contains("Stay hydrated!"))
    }

    @Test
    fun testRainConditions() {
        // Rain as main
        val recRain = WeatherRecommendation.getWorkoutRecommendation(18.0, "Rain", "light rain")
        assertTrue(recRain.contains("Rain detected!"))
        
        // Drizzle as main
        val recDrizzle = WeatherRecommendation.getWorkoutRecommendation(18.0, "Drizzle", "light drizzle")
        assertTrue(recDrizzle.contains("Rain detected!"))

        // Thunderstorm as main
        val recThunder = WeatherRecommendation.getWorkoutRecommendation(18.0, "Thunderstorm", "thunderstorm with rain")
        assertTrue(recThunder.contains("Rain detected!"))
    }

    @Test
    fun testColdWeather() {
        val recommendation = WeatherRecommendation.getWorkoutRecommendation(8.0, "Clear", "clear sky")
        assertTrue(recommendation.contains("Indoor workout"))
        assertTrue(recommendation.contains("Too cold"))
    }

    @Test
    fun testFogOrSmokeConditions() {
        // Fog as main
        val recFog = WeatherRecommendation.getWorkoutRecommendation(15.0, "Fog", "dense fog")
        assertTrue(recFog.contains("Indoors today due to air quality / low visibility"))

        // Smoke as main
        val recSmoke = WeatherRecommendation.getWorkoutRecommendation(15.0, "Smoke", "smoky air")
        assertTrue(recSmoke.contains("Indoors today due to air quality / low visibility"))

        // Haze as main
        val recHaze = WeatherRecommendation.getWorkoutRecommendation(15.0, "Haze", "haze")
        assertTrue(recHaze.contains("Indoors today due to air quality / low visibility"))

        // Mist as main
        val recMist = WeatherRecommendation.getWorkoutRecommendation(15.0, "Mist", "misty morning")
        assertTrue(recMist.contains("Indoors today due to air quality / low visibility"))

        // Foggy in description
        val recDescFoggy = WeatherRecommendation.getWorkoutRecommendation(15.0, "Clouds", "foggy sky")
        assertTrue(recDescFoggy.contains("Indoors today due to air quality / low visibility"))
    }

    @Test
    fun testDefaultWeather() {
        // 15°C, cloudy, no rain/fog
        val recommendation = WeatherRecommendation.getWorkoutRecommendation(15.0, "Clouds", "overcast clouds")
        assertTrue(recommendation.contains("Great day for a regular workout session!"))
    }
}
