package com.guiathayde.ping.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guiathayde.ping.data.remote.dto.MessageResponse
import com.guiathayde.ping.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    val messages = mutableStateListOf<MessageResponse>()
    var messageText by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var connectionError by mutableStateOf(false)

    val myUserId: String?
        get() = chatRepository.myUserId

    private var conversationId = ""
    private var observeJob: Job? = null

    fun loadMessages(conversationId: String) {
        this.conversationId = conversationId

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            chatRepository.observeMessages(conversationId).collect { cached ->
                messages.clear()
                messages.addAll(cached)
            }
        }

        viewModelScope.launch {
            isLoading = true
            connectionError = false
            try {
                chatRepository.refreshMessages(conversationId)
            } catch (e: Exception) {
                e.printStackTrace()
                connectionError = true
            }
            isLoading = false
        }
    }

    fun sendMessage() {
        val content = messageText.trim()
        if (content == "") return
        messageText = ""

        viewModelScope.launch {
            connectionError = false
            try {
                chatRepository.sendMessage(conversationId, content)
            } catch (e: Exception) {
                e.printStackTrace()
                connectionError = true
            }
        }
    }
}
