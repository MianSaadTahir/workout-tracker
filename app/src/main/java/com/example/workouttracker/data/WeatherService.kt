package com.example.workouttracker.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val main: MainData,
    val weather: List<WeatherData>,
    val name: String
)

data class MainData(
    val temp: Double
)

data class WeatherData(
    val main: String,
    val description: String,
    val icon: String
)

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

object WeatherRetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"
    
    // Put your OpenWeatherMap API key here
    const val API_KEY = "890309d8b37eda48a5d90d9dc1fdb528"

    var serviceOverride: WeatherApiService? = null

    val service: WeatherApiService
        get() = serviceOverride ?: lazyService

    private val lazyService: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
