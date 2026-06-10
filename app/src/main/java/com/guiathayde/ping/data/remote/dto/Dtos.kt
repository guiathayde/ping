package com.guiathayde.ping.data.remote.dto

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

data class MessageResponse(
    val id: String,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("sender_id") val senderId: String,
    val content: String,
    val timestamp: Long,
    val status: String
)

data class CreateConversationRequest(
    @SerializedName("participant_id") val participantId: String
)
