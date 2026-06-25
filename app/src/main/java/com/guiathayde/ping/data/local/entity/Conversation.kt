package com.guiathayde.ping.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.guiathayde.ping.data.remote.dto.ConversationResponse

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val participantId: String,
    val participantDisplayName: String,
    val participantUsername: String,
    val lastMessageContent: String?,
    val lastMessageTimestamp: Long?
)

fun ConversationResponse.toConversation(): Conversation = Conversation(
    id = id,
    participantId = participant?.id ?: "",
    participantDisplayName = participant?.displayName ?: "",
    participantUsername = participant?.username ?: "",
    lastMessageContent = lastMessage?.content,
    lastMessageTimestamp = lastMessage?.timestamp
)
