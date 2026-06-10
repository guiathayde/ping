package com.guiathayde.ping

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guiathayde.ping.ui.auth.AuthViewModel
import com.guiathayde.ping.ui.conversations.ConversationsViewModel


object AppViewModelProvider {
    val Factory = viewModelFactory {

        initializer {
            AuthViewModel(pingApplication().container.authRepository)
        }

        initializer {
            ConversationsViewModel(
                pingApplication().container.conversationsRepository,
                pingApplication().container.authRepository
            )
        }

    }
}

fun CreationExtras.pingApplication(): PingApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as PingApplication)
