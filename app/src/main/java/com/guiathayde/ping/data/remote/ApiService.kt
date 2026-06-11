package com.guiathayde.ping.data.remote

import com.guiathayde.ping.data.remote.dto.ConversationResponse
import com.guiathayde.ping.data.remote.dto.CreateConversationRequest
import com.guiathayde.ping.data.remote.dto.LoginRequest
import com.guiathayde.ping.data.remote.dto.LoginResponse
import com.guiathayde.ping.data.remote.dto.MessageResponse
import com.guiathayde.ping.data.remote.dto.SendMessageRequest
import com.guiathayde.ping.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("conversations")
    suspend fun getConversations(
        @Header("Authorization") token: String
    ): List<ConversationResponse>

    @GET("users/search")
    suspend fun searchUsers(
        @Header("Authorization") token: String,
        @Query("q") query: String
    ): List<UserResponse>

    @POST("conversations")
    suspend fun createConversation(
        @Header("Authorization") token: String,
        @Body request: CreateConversationRequest
    ): ConversationResponse

    @GET("conversations/{id}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("id") conversationId: String
    ): List<MessageResponse>

    @POST("conversations/{id}/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Path("id") conversationId: String,
        @Body request: SendMessageRequest
    ): MessageResponse
}
