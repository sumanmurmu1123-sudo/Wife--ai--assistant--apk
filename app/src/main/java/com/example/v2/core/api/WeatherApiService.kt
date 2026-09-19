package com.example.v2.core.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<OpenWeatherResponse>
}

data class OpenWeatherResponse(
    val name: String,
    val main: MainData,
    val weather: List<WeatherCondition>,
    val wind: WindData,
    val sys: SysData,
    val dt: Long
)

data class MainData(
    val temp: Float,
    val feels_like: Float,
    val humidity: Int
)

data class WeatherCondition(
    val main: String,
    val description: String,
    val icon: String
)

data class WindData(
    val speed: Float
)

data class SysData(
    val sunrise: Long,
    val sunset: Long
)
