package com.guiathayde.ping.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guiathayde.ping.data.remote.dto.UserResponse
import com.guiathayde.ping.data.repository.SearchRepository
import kotlinx.coroutines.launch

class SearchViewModel(private val searchRepository: SearchRepository) : ViewModel() {

    var query by mutableStateOf("")
    var results = mutableStateListOf<UserResponse>()
    var hasSearched by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var connectionError by mutableStateOf(false)
    var isConversationCreated by mutableStateOf(false)

    fun search() {
        val q = query.trim()
        if (q == "") return

        viewModelScope.launch {
            isLoading = true
            connectionError = false
            try {
                val rUsers = searchRepository.searchUsers(q)
                results.clear()
                rUsers.forEach { results.add(it) }
                hasSearched = true
            } catch (e: Exception) {
                e.printStackTrace()
                connectionError = true
            }
            isLoading = false
        }
    }

    fun startConversation(user: UserResponse) {
        viewModelScope.launch {
            isLoading = true
            connectionError = false
            try {
                searchRepository.createConversation(user.id)
                isConversationCreated = true
            } catch (e: Exception) {
                e.printStackTrace()
                connectionError = true
            }
            isLoading = false
        }
    }

    fun notifyTransition() {
        isConversationCreated = false
    }
}
