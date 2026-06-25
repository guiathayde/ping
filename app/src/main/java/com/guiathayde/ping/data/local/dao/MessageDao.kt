package com.guiathayde.ping.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.guiathayde.ping.data.remote.dto.MessageResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessages(conversationId: String): Flow<List<MessageResponse>>

    @Upsert
    suspend fun upsert(message: MessageResponse)

    @Upsert
    suspend fun upsertAll(messages: List<MessageResponse>)
}
