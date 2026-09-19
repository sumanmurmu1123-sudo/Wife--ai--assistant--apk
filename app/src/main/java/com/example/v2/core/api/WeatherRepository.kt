package com.example.v2.core.api

import com.example.v2.core.WeatherData
import com.example.v2.core.security.SecureStorage
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class WeatherRepository(private val secureStorage: SecureStorage) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create(WeatherApiService::class.java)

    suspend fun fetchWeather(lat: Double, lon: Double): Result<WeatherData> {
        val apiKey = secureStorage.getWeatherApiKey()
        if (apiKey.isNullOrBlank()) {
            return Result.failure(Exception("API_NOT_CONFIGURED"))
        }

        return try {
            val response = api.getCurrentWeather(lat, lon, apiKey)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(mapToWeatherData(body))
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToWeatherData(response: OpenWeatherResponse): WeatherData {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return WeatherData(
            locationName = response.name,
            temperature = response.main.temp,
            feelsLike = response.main.feels_like,
            condition = response.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Unknown",
            humidity = response.main.humidity,
            windSpeed = response.wind.speed * 3.6f, // Convert m/s to km/h
            precipitationProbability = 0, // OpenWeather current API doesn't always provide this in the simple response
            sunrise = timeFormat.format(Date(response.sys.sunrise * 1000)),
            sunset = timeFormat.format(Date(response.sys.sunset * 1000)),
            lastUpdated = System.currentTimeMillis()
        )
    }
}
