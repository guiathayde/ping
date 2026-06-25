package com.guiathayde.ping.data.repository

import com.guiathayde.ping.data.local.dao.ConversationDao
import com.guiathayde.ping.data.local.dao.MessageDao
import com.guiathayde.ping.data.local.entity.Conversation
import com.guiathayde.ping.data.local.entity.toConversation
import com.guiathayde.ping.data.remote.ApiService
import com.guiathayde.ping.data.remote.RetrofitInstance
import com.guiathayde.ping.data.remote.TokenManager
import kotlinx.coroutines.flow.Flow

class ConversationsRepository(
    private val tokenManager: TokenManager,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {

    private val client: ApiService = RetrofitInstance.api

    fun observeConversations(): Flow<List<Conversation>> =
        conversationDao.observeConversations()

    suspend fun refreshConversations() {
        val remote = client.getConversations("Bearer " + tokenManager.token)
        conversationDao.upsertAll(remote.map { it.toConversation() })
        val lastMessages = remote.mapNotNull { it.lastMessage }
        if (lastMessages.isNotEmpty()) {
            messageDao.upsertAll(lastMessages)
        }
    }
}
