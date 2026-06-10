package com.guiathayde.ping.data.remote

import com.guiathayde.ping.data.remote.dto.ConversationResponse
import com.guiathayde.ping.data.remote.dto.LoginRequest
import com.guiathayde.ping.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("conversations")
    suspend fun getConversations(
        @Header("Authorization") token: String
    ): List<ConversationResponse>
}
