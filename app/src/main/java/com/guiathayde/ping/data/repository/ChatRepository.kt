package com.guiathayde.ping.data.repository

import com.guiathayde.ping.data.remote.ApiService
import com.guiathayde.ping.data.remote.RetrofitInstance
import com.guiathayde.ping.data.remote.TokenManager
import com.guiathayde.ping.data.remote.dto.MessageResponse
import com.guiathayde.ping.data.remote.dto.SendMessageRequest

class ChatRepository(private val tokenManager: TokenManager) {

    private val client: ApiService = RetrofitInstance.api

    val myUserId: String?
        get() = tokenManager.userId

    suspend fun getMessages(conversationId: String): List<MessageResponse> {
        return client.getMessages("Bearer " + tokenManager.token, conversationId)
    }

    suspend fun sendMessage(conversationId: String, content: String): MessageResponse {
        return client.sendMessage(
            "Bearer " + tokenManager.token,
            conversationId,
            SendMessageRequest(content)
        )
    }
}
