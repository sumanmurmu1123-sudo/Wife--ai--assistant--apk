package com.example.v2.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.core.WeatherData
import com.example.v2.core.WeatherUiState
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.GlassBorder
import com.example.v2.ui.theme.NeonPink
import com.example.v2.ui.theme.Violet
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherCard(
    state: WeatherUiState,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Crossfade(targetState = state, label = "WeatherState") { currentState ->
                when (currentState) {
                    is WeatherUiState.Loading -> WeatherLoadingView()
                    is WeatherUiState.PermissionRequired -> WeatherErrorView(
                        message = "Location unavailable (Permission required)",
                        onRetry = onRequestPermission
                    )
                    is WeatherUiState.LocationServicesDisabled -> WeatherErrorView(
                        message = "Location unavailable (GPS disabled)",
                        onRetry = onRefresh
                    )
                    is WeatherUiState.LocationUnavailable -> WeatherErrorView(
                        message = "Location unavailable",
                        onRetry = onRefresh
                    )
                    is WeatherUiState.ApiNotConfigured -> WeatherActionView(
                        message = "Weather API not configured",
                        subMessage = "Please add an OpenWeatherMap API key in settings to enable weather.",
                        buttonText = "Setup API",
                        onAction = { /* Navigate to settings would happen here */ }
                    )
                    is WeatherUiState.Success -> WeatherSuccessView(
                        data = currentState.data,
                        onRefresh = onRefresh
                    )
                    is WeatherUiState.Error -> WeatherErrorView(
                        message = currentState.message,
                        onRetry = onRefresh
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherLoadingView() {
    Column(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Cyan, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Getting weather...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
    }
}

@Composable
private fun WeatherSuccessView(data: WeatherData, onRefresh: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Cyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(data.locationName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Text("Wife AI Weather", color = Cyan.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${data.temperature.toInt()}°C",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(data.condition, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Feels like ${data.feelsLike.toInt()}°C", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeatherDetailItem("Humidity", "${data.humidity}%")
            WeatherDetailItem("Wind", "${data.windSpeed.toInt()} km/h")
            WeatherDetailItem("Sunrise", data.sunrise)
            WeatherDetailItem("Sunset", data.sunset)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(data.lastUpdated))
        Text(
            "Last updated: $timeStr",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun WeatherDetailItem(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WeatherActionView(message: String, subMessage: String, buttonText: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = NeonPink, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(
            subMessage,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = Cyan),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(buttonText, color = DarkMidnightBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WeatherErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Unable to update weather", color = NeonPink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(message, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRetry) {
            Text("Retry", color = Cyan)
        }
    }
}
