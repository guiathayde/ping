package com.guiathayde.ping.ui.conversations

import com.guiathayde.ping.data.local.entity.Conversation
import com.guiathayde.ping.data.repository.AuthRepository
import com.guiathayde.ping.data.repository.ConversationsRepository
import com.guiathayde.ping.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ConversationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val conversationsRepository: ConversationsRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private fun conversation(id: String) =
        Conversation(id, "u2", "Bob", "bob", "hi", 1L)

    // O que: no init a ViewModel coleta o Flow do Room e espelha a lista no estado.
    // Caso de uso: ao abrir a tela, mostrar imediatamente as conversas em cache (offline-first).
    @Test
    fun init_observesConversationsFromRepository() = runTest {
        every { conversationsRepository.observeConversations() } returns
            flowOf(listOf(conversation("c1"), conversation("c2")))

        val viewModel = ConversationsViewModel(conversationsRepository, authRepository)

        assertEquals(2, viewModel.conversations.size)
        assertEquals("c1", viewModel.conversations[0].id)
    }

    // O que: refresh com sucesso limpa erro e loading e chama o repositorio.
    // Caso de uso: pull-to-refresh / abertura da tela sincroniza as conversas com o servidor.
    @Test
    fun loadConversations_success_clearsErrorAndLoading() = runTest {
        every { conversationsRepository.observeConversations() } returns flowOf(emptyList())
        coEvery { conversationsRepository.refreshConversations() } returns Unit
        val viewModel = ConversationsViewModel(conversationsRepository, authRepository)

        viewModel.loadConversations()

        assertFalse(viewModel.connectionError)
        assertFalse(viewModel.isLoading)
        coVerify { conversationsRepository.refreshConversations() }
    }

    // O que: falha no refresh liga connectionError e desliga loading.
    // Caso de uso: sem rede ao atualizar -> sinalizar erro sem travar a tela (cache continua visivel).
    @Test
    fun loadConversations_error_setsConnectionError() = runTest {
        every { conversationsRepository.observeConversations() } returns flowOf(emptyList())
        coEvery { conversationsRepository.refreshConversations() } throws RuntimeException("net")
        val viewModel = ConversationsViewModel(conversationsRepository, authRepository)

        viewModel.loadConversations()

        assertTrue(viewModel.connectionError)
        assertFalse(viewModel.isLoading)
    }

    // O que: logout da ViewModel delega para o AuthRepository.
    // Caso de uso: usuario toca em "Sair" -> encerrar a sessao (token + cache local).
    @Test
    fun logout_delegatesToAuthRepository() {
        every { conversationsRepository.observeConversations() } returns flowOf(emptyList())
        val viewModel = ConversationsViewModel(conversationsRepository, authRepository)

        viewModel.logout()

        verify { authRepository.logout() }
    }
}
