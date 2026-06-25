package com.guiathayde.ping.ui.conversations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guiathayde.ping.data.local.entity.Conversation
import com.guiathayde.ping.data.repository.AuthRepository
import com.guiathayde.ping.data.repository.ConversationsRepository
import kotlinx.coroutines.launch

class ConversationsViewModel(
    private val conversationsRepository: ConversationsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val conversations = mutableStateListOf<Conversation>()
    var isLoading by mutableStateOf(false)
    var connectionError by mutableStateOf(false)

    init {
        viewModelScope.launch {
            conversationsRepository.observeConversations().collect { cached ->
                conversations.clear()
                conversations.addAll(cached)
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            isLoading = true
            connectionError = false
            try {
                conversationsRepository.refreshConversations()
            } catch (e: Exception) {
                e.printStackTrace()
                connectionError = true
            }
            isLoading = false
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
