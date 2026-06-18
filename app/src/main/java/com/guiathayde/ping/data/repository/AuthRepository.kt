package com.guiathayde.ping.data.repository

import com.guiathayde.ping.data.remote.ApiService
import com.guiathayde.ping.data.remote.RetrofitInstance
import com.guiathayde.ping.data.remote.TokenManager
import com.guiathayde.ping.data.remote.WebSocketManager
import com.guiathayde.ping.data.remote.dto.LoginRequest

class AuthRepository(
    private val tokenManager: TokenManager,
    private val webSocketManager: WebSocketManager
) {

    private val client: ApiService = RetrofitInstance.api

    val isLoggedIn: Boolean
        get() = tokenManager.isLoggedIn

    suspend fun login(displayName: String, username: String): String {
        return try {
            val res = client.login(LoginRequest(displayName, username))
            tokenManager.token = res.token
            tokenManager.userId = res.user.id
            tokenManager.displayName = res.user.displayName
            tokenManager.username = res.user.username
            webSocketManager.connect()
            "success"
        } catch (e: Exception) {
            e.printStackTrace()
            "failed_connection"
        }
    }

    fun logout() {
        webSocketManager.disconnect()
        tokenManager.clear()
    }

    fun connectIfAuthenticated() {
        if (tokenManager.isLoggedIn) {
            webSocketManager.connect()
        }
    }
}
