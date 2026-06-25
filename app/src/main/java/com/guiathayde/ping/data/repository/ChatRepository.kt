package com.guiathayde.ping.data.repository

import com.guiathayde.ping.data.local.dao.ConversationDao
import com.guiathayde.ping.data.local.dao.MessageDao
import com.guiathayde.ping.data.remote.ApiService
import com.guiathayde.ping.data.remote.RetrofitInstance
import com.guiathayde.ping.data.remote.TokenManager
import com.guiathayde.ping.data.remote.dto.MessageResponse
import com.guiathayde.ping.data.remote.dto.SendMessageRequest
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val tokenManager: TokenManager,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) {

    private val client: ApiService = RetrofitInstance.api

    val myUserId: String?
        get() = tokenManager.userId

    fun observeMessages(conversationId: String): Flow<List<MessageResponse>> =
        messageDao.observeMessages(conversationId)

    suspend fun refreshMessages(conversationId: String) {
        val remote = client.getMessages("Bearer " + tokenManager.token, conversationId)
        messageDao.upsertAll(remote)
    }

    suspend fun sendMessage(conversationId: String, content: String) {
        val message = client.sendMessage(
            "Bearer " + tokenManager.token,
            conversationId,
            SendMessageRequest(content)
        )
        persistMessage(message)
    }

    suspend fun saveIncomingMessage(message: MessageResponse): Boolean {
        messageDao.upsert(message)
        val known = conversationDao.getById(message.conversationId) != null
        if (known) {
            conversationDao.updateLastMessage(message.conversationId, message.content, message.timestamp)
        }
        return known
    }

    private suspend fun persistMessage(message: MessageResponse) {
        messageDao.upsert(message)
        conversationDao.updateLastMessage(message.conversationId, message.content, message.timestamp)
    }
}
