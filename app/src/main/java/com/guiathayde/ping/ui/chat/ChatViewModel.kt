package com.guiathayde.ping.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guiathayde.ping.data.remote.dto.MessageResponse
import com.guiathayde.ping.data.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    var messages = mutableStateListOf<MessageResponse>()
    var messageText by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var connectionError by mutableStateOf(false)

    val myUserId: String?
        get() = chatRepository.myUserId

    private var conversationId = ""

    fun loadMessages(conversationId: String) {
        this.conversationId = conversationId
        viewModelScope.launch {
            isLoading = true
            connectionError = false
            try {
                val rMessages = chatRepository.getMessages(conversationId)
                messages.clear()
                rMessages.forEach { messages.add(it) }
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

        viewModelScope.launch {
            connectionError = false
            try {
                val newMessage = chatRepository.sendMessage(conversationId, content)
                messages.add(newMessage)
                messageText = ""
            } catch (e: Exception) {
                e.printStackTrace()
                connectionError = true
            }
        }
    }
}
