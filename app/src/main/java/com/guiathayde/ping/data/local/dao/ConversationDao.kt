package com.guiathayde.ping.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.guiathayde.ping.data.local.entity.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY lastMessageTimestamp DESC")
    fun observeConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): Conversation?

    @Upsert
    suspend fun upsert(conversation: Conversation)

    @Upsert
    suspend fun upsertAll(conversations: List<Conversation>)

    @Query(
        """
        UPDATE conversations
        SET lastMessageContent = :content, lastMessageTimestamp = :timestamp
        WHERE id = :conversationId
          AND (lastMessageTimestamp IS NULL OR lastMessageTimestamp <= :timestamp)
        """
    )
    suspend fun updateLastMessage(conversationId: String, content: String, timestamp: Long)
}
