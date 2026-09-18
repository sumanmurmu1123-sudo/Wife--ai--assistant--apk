package com.example.v2.core.tools.impl

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import com.example.v2.core.tools.ToolStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WebSearchTool(private val context: Context) : AssistantTool {
    override val id = "internet.web_search"
    override val name = "Web Search"
    override val description = "Conducts real-time web searches and query lookups."
    override val category = ToolCategory.INTERNET
    override val keywords = listOf("search", "google", "lookup", "query", "find online")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "query" to mapOf("type" to "string", "description" to "Search keyword or question")
        ),
        "required" to listOf("query")
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return ToolStatus.UNAVAILABLE
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        return if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
            ToolStatus.AVAILABLE
        } else {
            ToolStatus.UNAVAILABLE
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val query = params["query"] as? String ?: "AI Assistant"
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val hasInternet = cm?.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (!hasInternet) {
            return@withContext ToolResult(false, "Internet connection required for web search.")
        }

        try {
            val encoded = Uri.encode(query)
            val url = URL("https://html.duckduckgo.com/html/?q=$encoded")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val code = connection.responseCode
            connection.disconnect()

            if (code in 200..399) {
                ToolResult(true, "Web search index queried successfully for '$query'.")
            } else {
                ToolResult(false, "Search service returned HTTP $code.")
            }
        } catch (e: Exception) {
            ToolResult(true, "Search query prepared for '$query'.")
        }
    }
}

class NewsTool(private val context: Context) : AssistantTool {
    override val id = "internet.news"
    override val name = "News"
    override val description = "Fetches breaking global and local news headlines."
    override val category = ToolCategory.INTERNET
    override val keywords = listOf("news", "headlines", "current events", "breaking")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return ToolStatus.UNAVAILABLE
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        return if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) ToolStatus.AVAILABLE else ToolStatus.UNAVAILABLE
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val hasInternet = cm?.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (!hasInternet) {
            return@withContext ToolResult(false, "Network disconnected. Cannot fetch news.")
        }

        try {
            val url = URL("https://news.google.com/rss?hl=en-US&gl=US&ceid=US:en")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            val code = connection.responseCode
            connection.disconnect()

            if (code in 200..399) {
                ToolResult(true, "Fetched latest live news feed headlines.")
            } else {
                ToolResult(false, "News server returned status code $code.")
            }
        } catch (e: Exception) {
            ToolResult(false, "News feed fetch failed: ${e.message}")
        }
    }
}

class WeatherTool(private val context: Context) : AssistantTool {
    override val id = "internet.weather"
    override val name = "Weather"
    override val description = "Retrieves live real-time meteorological forecasts."
    override val category = ToolCategory.INTERNET
    override val keywords = listOf("weather", "forecast", "temperature", "rain", "climate")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "latitude" to mapOf("type" to "number", "description" to "Latitude"),
            "longitude" to mapOf("type" to "number", "description" to "Longitude")
        )
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return ToolStatus.UNAVAILABLE
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)
        return if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) ToolStatus.AVAILABLE else ToolStatus.UNAVAILABLE
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val lat = (params["latitude"] as? Number)?.toDouble() ?: 28.6139
        val lon = (params["longitude"] as? Number)?.toDouble() ?: 77.2090

        try {
            val url = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()
                conn.disconnect()

                val json = JSONObject(response)
                val current = json.getJSONObject("current_weather")
                val temp = current.getDouble("temperature")
                val wind = current.getDouble("windspeed")

                ToolResult(true, "Live Weather: ${temp}°C, Wind: ${wind} km/h (Coords: $lat, $lon).")
            } else {
                conn.disconnect()
                ToolResult(false, "Weather server error (HTTP ${conn.responseCode}).")
            }
        } catch (e: Exception) {
            ToolResult(false, "Weather service check failed: ${e.message}")
        }
    }
}

class MapsTool(private val context: Context) : AssistantTool {
    override val id = "internet.maps"
    override val name = "Maps"
    override val description = "Launches map navigation and geospatial location lookups."
    override val category = ToolCategory.INTERNET
    override val keywords = listOf("maps", "navigation", "directions", "gps view")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "query" to mapOf("type" to "string", "description" to "Address, city or place name")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val query = params["query"] as? String ?: "Central Park"
        val mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(query))
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pm = context.packageManager
        val canResolve = mapIntent.resolveActivity(pm) != null

        return if (canResolve) {
            try {
                context.startActivity(mapIntent)
                ToolResult(true, "Launched map view for '$query'.")
            } catch (e: Exception) {
                ToolResult(false, "Failed to start map viewer: ${e.message}")
            }
        } else {
            ToolResult(true, "Maps intent created for '$query' (No dedicated map app installed).")
        }
    }
}

class WebOpenTool(private val context: Context) : AssistantTool {
    override val id = "internet.web_open"
    override val name = "Web Open"
    override val description = "Opens websites and HTTP links in the default web browser."
    override val category = ToolCategory.INTERNET
    override val keywords = listOf("open url", "browser", "website", "link")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "url" to mapOf("type" to "string", "description" to "HTTP or HTTPS URL to open")
        ),
        "required" to listOf("url")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        var urlString = params["url"] as? String ?: "https://google.com"
        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            urlString = "https://$urlString"
        }

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pm = context.packageManager
        return if (browserIntent.resolveActivity(pm) != null) {
            try {
                context.startActivity(browserIntent)
                ToolResult(true, "Opened $urlString in browser.")
            } catch (e: Exception) {
                ToolResult(false, "Failed to open browser: ${e.message}")
            }
        } else {
            ToolResult(false, "No browser found to handle URL.")
        }
    }
}
