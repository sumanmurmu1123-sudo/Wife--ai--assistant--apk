package com.example.v2.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.v2.core.GeminiConnectionState
import com.example.v2.core.ServiceConnectionState
import com.example.v2.core.StateManager
import com.example.v2.core.api.GeminiRepository
import com.example.v2.core.security.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ApiCloudViewModel(application: Application) : AndroidViewModel(application) {
    private val core = com.example.v2.core.WifeAssistantCore.getInstance(application)
    private val secureStorage = core.secureStorage
    private val geminiRepository = core.geminiRepository
    private val elevenLabsRepository = core.elevenLabsRepository

    private val _apiKey = MutableStateFlow(secureStorage.getApiKey() ?: "")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _elevenLabsKey = MutableStateFlow(secureStorage.getElevenLabsKey() ?: "")
    val elevenLabsKey: StateFlow<String> = _elevenLabsKey.asStateFlow()

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    private val _elevenLabsTestResult = MutableStateFlow<String?>(null)
    val elevenLabsTestResult: StateFlow<String?> = _elevenLabsTestResult.asStateFlow()

    val connectionState = StateManager.state

    fun saveApiKey(key: String) {
        val trimmedKey = key.trim()
        if (trimmedKey.isNotEmpty()) {
            secureStorage.saveApiKey(trimmedKey)
            _apiKey.value = trimmedKey
            _testResult.value = null
            // Reset state to disconnected when key changes
            StateManager.updateState { it.copy(geminiState = GeminiConnectionState.DISCONNECTED) }
        }
    }

    fun saveElevenLabsKey(key: String) {
        val trimmedKey = key.trim()
        if (trimmedKey.isNotEmpty()) {
            secureStorage.saveElevenLabsKey(trimmedKey)
            _elevenLabsKey.value = trimmedKey
            _elevenLabsTestResult.value = null
            StateManager.updateState { it.copy(elevenLabsState = ServiceConnectionState.DISCONNECTED) }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _testResult.value = null
            val result = geminiRepository.testConnection()
            result.onFailure {
                _testResult.value = "ERROR: ${it.message}"
            }
            result.onSuccess {
                _testResult.value = "CONNECTED"
            }
        }
    }

    fun testElevenLabsConnection() {
        viewModelScope.launch {
            _elevenLabsTestResult.value = null
            val result = elevenLabsRepository.testConnection()
            result.onFailure {
                _elevenLabsTestResult.value = "ERROR: ${it.message}"
            }
            result.onSuccess {
                _elevenLabsTestResult.value = "CONNECTED"
            }
        }
    }
    
    fun clearApiKey() {
        secureStorage.clearApiKey()
        _apiKey.value = ""
        StateManager.updateState { it.copy(geminiState = GeminiConnectionState.DISCONNECTED) }
    }

    fun clearElevenLabsKey() {
        secureStorage.clearElevenLabsKey()
        _elevenLabsKey.value = ""
        StateManager.updateState { it.copy(elevenLabsState = ServiceConnectionState.NOT_CONFIGURED) }
    }
}
