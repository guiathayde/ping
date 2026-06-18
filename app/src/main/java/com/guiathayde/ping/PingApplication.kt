package com.guiathayde.ping

import android.app.Application
import android.content.Context
import com.guiathayde.ping.data.remote.TokenManager
import com.guiathayde.ping.data.remote.WebSocketManager
import com.guiathayde.ping.data.repository.AuthRepository
import com.guiathayde.ping.data.repository.ChatRepository
import com.guiathayde.ping.data.repository.ConversationsRepository
import com.guiathayde.ping.data.repository.SearchRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class PingApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(private val context: Context) {
    val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .pingInterval(20, TimeUnit.SECONDS)
            .build()
    }

    val webSocketManager: WebSocketManager by lazy {
        WebSocketManager(tokenManager, okHttpClient)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(tokenManager, webSocketManager)
    }

    val conversationsRepository: ConversationsRepository by lazy {
        ConversationsRepository(tokenManager, webSocketManager)
    }

    val searchRepository: SearchRepository by lazy {
        SearchRepository(tokenManager)
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(tokenManager, webSocketManager)
    }
}
