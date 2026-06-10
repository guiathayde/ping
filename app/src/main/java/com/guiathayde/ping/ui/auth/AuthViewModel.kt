package com.guiathayde.ping.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guiathayde.ping.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(val authRepository: AuthRepository) : ViewModel() {

    var displayName by mutableStateOf("")
    var username by mutableStateOf("")
    var fieldsError by mutableStateOf(false)
    var connectionError by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var isLoginSuccessful by mutableStateOf(false)

    fun performLogin() {
        fieldsError = false
        connectionError = false

        displayName = displayName.trim()
        username = username.trim()

        if (displayName == "" || username == "") {
            fieldsError = true
            return
        }

        viewModelScope.launch {
            isLoading = true
            val status = authRepository.login(displayName, username)
            isLoading = false
            when (status) {
                "success" -> isLoginSuccessful = true
                else -> connectionError = true
            }
        }
    }

    fun notifyTransition() {
        isLoginSuccessful = false
    }
}
