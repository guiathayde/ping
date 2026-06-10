package com.guiathayde.ping

import android.app.Application
import android.content.Context
import com.guiathayde.ping.data.remote.TokenManager
import com.guiathayde.ping.data.repository.AuthRepository

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

    val authRepository: AuthRepository by lazy {
        AuthRepository(tokenManager)
    }
}
