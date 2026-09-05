package com.example.domain

import com.example.data.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object ChatStateManager {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun addMessage(message: ChatMessage) {
        _messages.update { it + message }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun setTyping(typing: Boolean) {
        _isTyping.value = typing
    }
}
