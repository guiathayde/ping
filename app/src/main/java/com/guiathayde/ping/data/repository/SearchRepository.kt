package com.guiathayde.ping.data.repository

import com.guiathayde.ping.data.remote.ApiService
import com.guiathayde.ping.data.remote.RetrofitInstance
import com.guiathayde.ping.data.remote.TokenManager
import com.guiathayde.ping.data.remote.dto.ConversationResponse
import com.guiathayde.ping.data.remote.dto.CreateConversationRequest
import com.guiathayde.ping.data.remote.dto.UserResponse

class SearchRepository(private val tokenManager: TokenManager) {

    private val client: ApiService = RetrofitInstance.api

    suspend fun searchUsers(query: String): List<UserResponse> {
        return client.searchUsers("Bearer " + tokenManager.token, query)
    }

    suspend fun createConversation(participantId: String): ConversationResponse {
        return client.createConversation(
            "Bearer " + tokenManager.token,
            CreateConversationRequest(participantId)
        )
    }
}
