package com.guiathayde.ping.data.remote.dto

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("display_name") val displayName: String,
    val username: String
)

data class LoginResponse(
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val id: String,
    @SerializedName("display_name") val displayName: String,
    val username: String
)

data class ConversationResponse(
    val id: String,
    val participant: UserResponse?,
    @SerializedName("last_message") val lastMessage: MessageResponse?
)

@Entity(tableName = "messages", indices = [Index("conversationId")])
data class MessageResponse(
    @PrimaryKey val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("sender_id") val senderId: String,
    val content: String,
    val timestamp: Long,
    val status: String
)

data class CreateConversationRequest(
    @SerializedName("participant_id") val participantId: String
)

data class SendMessageRequest(
    val content: String
)
