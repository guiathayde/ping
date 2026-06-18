package com.guiathayde.ping.data.repository

import com.guiathayde.ping.data.remote.ApiService
import com.guiathayde.ping.data.remote.RetrofitInstance
import com.guiathayde.ping.data.remote.TokenManager
import com.guiathayde.ping.data.remote.WebSocketManager
import com.guiathayde.ping.data.remote.dto.ConversationResponse
import com.guiathayde.ping.data.remote.dto.MessageResponse
import kotlinx.coroutines.flow.SharedFlow

class ConversationsRepository(
    private val tokenManager: TokenManager,
    private val webSocketManager: WebSocketManager
) {

    private val client: ApiService = RetrofitInstance.api

    val incomingMessages: SharedFlow<MessageResponse>
        get() = webSocketManager.incomingMessages

    suspend fun getConversations(): List<ConversationResponse> {
        return client.getConversations("Bearer " + tokenManager.token)
    }
}
