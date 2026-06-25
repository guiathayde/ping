package com.guiathayde.ping

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.guiathayde.ping.data.local.AppDatabase
import com.guiathayde.ping.data.local.dao.ConversationDao
import com.guiathayde.ping.data.local.dao.MessageDao
import com.guiathayde.ping.data.remote.TokenManager
import com.guiathayde.ping.data.remote.WebSocketManager
import com.guiathayde.ping.data.repository.AuthRepository
import com.guiathayde.ping.data.repository.ChatRepository
import com.guiathayde.ping.data.repository.ConversationsRepository
import com.guiathayde.ping.data.repository.SearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class PingApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.startMessageSync()
    }
}

class AppContainer(private val context: Context) {

    val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "ping.db").build()
    }
    private val conversationDao: ConversationDao by lazy { appDatabase.conversationDao() }
    private val messageDao: MessageDao by lazy { appDatabase.messageDao() }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .pingInterval(20, TimeUnit.SECONDS)
            .build()
    }

    val webSocketManager: WebSocketManager by lazy {
        WebSocketManager(tokenManager, okHttpClient)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(tokenManager, webSocketManager, appDatabase, applicationScope)
    }

    val conversationsRepository: ConversationsRepository by lazy {
        ConversationsRepository(tokenManager, conversationDao, messageDao)
    }

    val searchRepository: SearchRepository by lazy {
        SearchRepository(tokenManager, conversationDao, messageDao)
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(tokenManager, messageDao, conversationDao)
    }

    fun startMessageSync() {
        applicationScope.launch {
            webSocketManager.incomingMessages.collect { message ->
                val known = chatRepository.saveIncomingMessage(message)
                if (!known) {
                    runCatching { conversationsRepository.refreshConversations() }
                }
            }
        }
    }
}
