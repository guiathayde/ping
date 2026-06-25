package com.guiathayde.ping.data.repository

import com.guiathayde.ping.data.local.dao.ConversationDao
import com.guiathayde.ping.data.local.dao.MessageDao
import com.guiathayde.ping.data.local.entity.toConversation
import com.guiathayde.ping.data.remote.ApiService
import com.guiathayde.ping.data.remote.RetrofitInstance
import com.guiathayde.ping.data.remote.TokenManager
import com.guiathayde.ping.data.remote.dto.ConversationResponse
import com.guiathayde.ping.data.remote.dto.CreateConversationRequest
import com.guiathayde.ping.data.remote.dto.UserResponse

class SearchRepository(
    private val tokenManager: TokenManager,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {

    private val client: ApiService = RetrofitInstance.api

    suspend fun searchUsers(query: String): List<UserResponse> {
        return client.searchUsers("Bearer " + tokenManager.token, query)
    }

    suspend fun createConversation(participantId: String): ConversationResponse {
        val conversation = client.createConversation(
            "Bearer " + tokenManager.token,
            CreateConversationRequest(participantId)
        )
        conversationDao.upsert(conversation.toConversation())
        conversation.lastMessage?.let { messageDao.upsert(it) }
        return conversation
    }
}
