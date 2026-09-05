package com.example.ui.webdesign

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebDesignStudio(
    initialHtmlCode: String = defaultWebTemplate,
    onClose: () -> Unit
) {
    var htmlCode by remember { mutableStateOf(initialHtmlCode) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isEditingCode by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090912))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Web,
                        contentDescription = "Web Hub",
                        tint = Color(0xFF00F5FF)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wife Web Studio",
                        color = Color.White,
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { isEditingCode = !isEditingCode }) {
                        Icon(
                            imageVector = if (isEditingCode) Icons.Default.Visibility else Icons.Default.Code,
                            contentDescription = "Toggle Editor",
                            tint = Color(0xFFFF007F)
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }
            }

            // Split View / Toggle View: Live Preview vs Code Editor
            if (isEditingCode) {
                OutlinedTextField(
                    value = htmlCode,
                    onValueChange = {
                        htmlCode = it
                        webViewInstance?.loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141420)),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color(0xFF00F5FF)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00F5FF),
                        unfocusedBorderColor = Color(0xFF2A2A3E)
                    )
                )
            } else {
                // Live Interactive Web Design Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Brush.linearGradient(listOf(Color(0xFF00F5FF), Color(0xFFFF007F))), RoundedCornerShape(16.dp))
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                                loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
                                webViewInstance = this
                            }
                        },
                        update = { view ->
                            view.loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
                        }
                    )
                }
            }
        }
    }
}

// Default Modern Cyberpunk Landing Page Template
val defaultWebTemplate = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            margin: 0;
            background: radial-gradient(circle, #1a0b2e 0%, #06060c 100%);
            color: #fff;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100vh;
            text-align: center;
        }
        h1 {
            font-size: 2.2rem;
            background: linear-gradient(90deg, #00f5ff, #ff007f);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin-bottom: 8px;
        }
        p { color: #8a8aa3; font-size: 0.95rem; margin-bottom: 24px; padding: 0 15px; }
        .btn {
            background: linear-gradient(45deg, #00f5ff, #0072ff);
            border: none;
            color: #000;
            padding: 12px 28px;
            font-size: 1rem;
            font-weight: bold;
            border-radius: 25px;
            box-shadow: 0 0 15px rgba(0, 245, 255, 0.4);
            cursor: pointer;
        }
    </style>
</head>
<body>
    <h1>Designed with Wife AI</h1>
    <p>Modern, high-performance responsive web application design.</p>
    <button class="btn" onclick="alert('Connected to Wife Assistant!')">Explore Design</button>
</body>
</html>
""".trimIndent()
