package com.guiathayde.ping.data.remote

import com.google.gson.Gson
import com.guiathayde.ping.data.remote.dto.MessageResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketManager(
    private val tokenManager: TokenManager,
    private val client: OkHttpClient
) {
    companion object {
        private const val WS_URL = "ws://localhost:3000/ws"
    }

    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _incomingMessages = MutableSharedFlow<MessageResponse>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val incomingMessages: SharedFlow<MessageResponse> = _incomingMessages.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var intentionalClose = false
    private var reconnectAttempts = 0

    @Synchronized
    fun connect() {
        if (webSocket != null) return
        val token = tokenManager.token ?: return

        intentionalClose = false

        val request = Request.Builder()
            .url("$WS_URL?token=$token")
            .build()

        webSocket = client.newWebSocket(request, listener)
    }

    @Synchronized
    fun disconnect() {
        intentionalClose = true
        reconnectAttempts = 0
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            reconnectAttempts = 0
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val envelope = gson.fromJson(text, WsEnvelope::class.java)
                if (envelope?.type == "new_message" && envelope.data != null) {
                    _incomingMessages.tryEmit(envelope.data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            handleDisconnect(code)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()
            handleDisconnect(null)
        }
    }

    @Synchronized
    private fun handleDisconnect(code: Int?) {
        webSocket = null

        // Fechamento intencional ou token invalido (1008) -> nao reconectar.
        if (intentionalClose || code == 1008) return
        if (tokenManager.token == null) return

        scheduleReconnect()
    }

    private fun scheduleReconnect() {
        val attempt = reconnectAttempts++
        // Backoff exponencial: 1s, 2s, 4s, 8s, 16s, 32s (teto 30s).
        val backoffMs = minOf(30_000L, 1_000L * (1L shl minOf(attempt, 5)))
        scope.launch {
            delay(backoffMs)
            if (!intentionalClose && tokenManager.token != null) {
                connect()
            }
        }
    }
}

private data class WsEnvelope(
    val type: String?,
    val data: MessageResponse?
)
