package com.example.v2.ai

import android.util.Base64
import com.example.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.json.JSONArray
import org.json.JSONObject

class GeminiLiveManager {
    private val client = HttpClient(OkHttp) {
        install(WebSockets) {
        }
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var webSocketSession: WebSocketSession? = null
    
    private val _audioFlow = MutableSharedFlow<ByteArray>(replay = 0, extraBufferCapacity = 50)
    val audioFlow: SharedFlow<ByteArray> = _audioFlow
    
    private val _turnCompleteFlow = MutableSharedFlow<Unit>()
    val turnCompleteFlow: SharedFlow<Unit> = _turnCompleteFlow

    private val _functionCallFlow = MutableSharedFlow<JSONObject>()
    val functionCallFlow: SharedFlow<JSONObject> = _functionCallFlow

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow

    private fun mapToJsonObject(map: Map<String, Any?>): JSONObject {
        val json = JSONObject()
        map.forEach { (key, value) ->
            json.put(key, when (value) {
                is Map<*, *> -> mapToJsonObject(value as Map<String, Any?>)
                is List<*> -> listToJsonArray(value)
                else -> value
            })
        }
        return json
    }

    private fun listToJsonArray(list: List<*>): JSONArray {
        val array = JSONArray()
        list.forEach { value ->
            array.put(when (value) {
                is Map<*, *> -> mapToJsonObject(value as Map<String, Any?>)
                is List<*> -> listToJsonArray(value)
                else -> value
            })
        }
        return array
    }

    suspend fun connect(systemInstruction: String = "", apiKeyOverride: String? = null, dynamicTools: List<com.example.v2.core.tools.AssistantTool> = emptyList()) {
        try {
            val apiKey = if (!apiKeyOverride.isNullOrBlank()) apiKeyOverride else BuildConfig.GEMINI_API_KEY
            val host = "generativelanguage.googleapis.com"
            val path = "/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"
            
            webSocketSession = client.webSocketSession(
                method = HttpMethod.Get,
                host = host,
                path = "$path?key=$apiKey",
                port = 443
            ) {
                url.protocol = io.ktor.http.URLProtocol.WSS
            }
            
            // Send setup
            val setupMessage = JSONObject().apply {
                put("setup", JSONObject().apply {
                    put("model", "models/gemini-2.0-flash-exp")
                    
                    put("tools", JSONArray().apply {
                        put(JSONObject().apply {
                            put("functionDeclarations", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("name", "initiate_upi_payment")
                                    put("description", "Initiate a UPI payment to a specified user. Use this when the user asks to pay or send money.")
                                    put("parameters", JSONObject().apply {
                                        put("type", "OBJECT")
                                        put("properties", JSONObject().apply {
                                            put("amount", JSONObject().apply { put("type", "NUMBER") })
                                            put("recipientName", JSONObject().apply { put("type", "STRING") })
                                            put("upiId", JSONObject().apply { put("type", "STRING"); put("description", "UPI ID of the recipient. If unknown, ask the user or leave empty.") })
                                        })
                                        put("required", JSONArray().apply { put("amount"); put("recipientName") })
                                    })
                                })
                                put(JSONObject().apply {
                                    put("name", "open_instagram_reel_creator")
                                    put("description", "Open the Instagram Reel Creator feature. Use this when the user asks to edit a video for Instagram, create a reel, generate viral captions or hashtags for a video.")
                                })
                                put(JSONObject().apply {
                                    put("name", "open_opportunity_center")
                                    put("description", "Open the RIX Opportunity Center. Use this when the user asks for business opportunities, freelance jobs, or their daily business briefing.")
                                })
                                // Add dynamic tools from registry
                                dynamicTools.forEach { tool ->
                                    put(JSONObject().apply {
                                        put("name", tool.id.replace(".", "_"))
                                        put("description", tool.description)
                                        put("parameters", mapToJsonObject(tool.parametersSchema))
                                    })
                                }
                            })
                        })
                    })

                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemInstruction)
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply {
                            put("AUDIO")
                        })
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", "Aoede") // Female voice
                                })
                            })
                        })
                    })
                })
            }
            webSocketSession?.send(Frame.Text(setupMessage.toString()))
            
            // Listen to responses
            scope.launch {
                listenForMessages()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            scope.launch {
                _errorFlow.emit(e.message ?: "Connection failed")
            }
        }
    }
    
    private suspend fun listenForMessages() {
        val session = webSocketSession ?: return
        try {
            for (frame in session.incoming) {
                if (frame !is Frame.Text) continue
                val text = frame.readText()
                val json = JSONObject(text)
                
                if (json.has("serverContent")) {
                    val serverContent = json.getJSONObject("serverContent")
                    if (serverContent.has("interrupted") && serverContent.getBoolean("interrupted")) {
                        _turnCompleteFlow.emit(Unit)
                    }
                    if (serverContent.has("modelTurn")) {
                        val parts = serverContent.getJSONObject("modelTurn").getJSONArray("parts")
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            if (part.has("inlineData")) {
                                val inlineData = part.getJSONObject("inlineData")
                                val data = inlineData.getString("data")
                                val decoded = Base64.decode(data, Base64.DEFAULT)
                                _audioFlow.emit(decoded)
                            }
                            if (part.has("functionCall")) {
                                val functionCall = part.getJSONObject("functionCall")
                                _functionCallFlow.emit(functionCall)
                            }
                        }
                    }
                    if (serverContent.has("turnComplete") && serverContent.getBoolean("turnComplete")) {
                        _turnCompleteFlow.emit(Unit)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _errorFlow.emit("Connection closed unexpectedly")
        }
    }
    
    suspend fun sendAudioChunk(pcmData: ByteArray) {
        val base64Data = Base64.encodeToString(pcmData, Base64.NO_WRAP)
        val message = JSONObject().apply {
            put("realtimeInput", JSONObject().apply {
                put("mediaChunks", JSONArray().apply {
                    put(JSONObject().apply {
                        put("mimeType", "audio/pcm;rate=16000")
                        put("data", base64Data)
                    })
                })
            })
        }
        webSocketSession?.send(Frame.Text(message.toString()))
    }
    
    suspend fun sendClientContentMessage(message: String) {
        val json = JSONObject().apply {
            put("clientContent", JSONObject().apply {
                put("turns", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", message)
                            })
                        })
                    })
                })
                put("turnComplete", true)
            })
        }
        webSocketSession?.send(Frame.Text(json.toString()))
    }
    
    suspend fun disconnect() {
        webSocketSession?.close()
        webSocketSession = null
    }
}
