package com.guiathayde.ping.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.guiathayde.ping.data.local.dao.ConversationDao
import com.guiathayde.ping.data.local.dao.MessageDao
import com.guiathayde.ping.data.local.entity.Conversation
import com.guiathayde.ping.data.remote.dto.MessageResponse

@Database(
    entities = [Conversation::class, MessageResponse::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
