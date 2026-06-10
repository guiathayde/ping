package com.guiathayde.ping.data.repository

import com.guiathayde.ping.data.remote.ApiService
import com.guiathayde.ping.data.remote.RetrofitInstance
import com.guiathayde.ping.data.remote.TokenManager
import com.guiathayde.ping.data.remote.dto.ConversationResponse

class ConversationsRepository(private val tokenManager: TokenManager) {

    private val client: ApiService = RetrofitInstance.api

    suspend fun getConversations(): List<ConversationResponse> {
        return client.getConversations("Bearer " + tokenManager.token)
    }
}
