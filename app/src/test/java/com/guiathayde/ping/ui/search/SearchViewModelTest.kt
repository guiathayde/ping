package com.guiathayde.ping.ui.search

import com.guiathayde.ping.data.remote.dto.ConversationResponse
import com.guiathayde.ping.data.remote.dto.UserResponse
import com.guiathayde.ping.data.repository.SearchRepository
import com.guiathayde.ping.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchRepository: SearchRepository = mockk(relaxed = true)
    private val viewModel = SearchViewModel(searchRepository)

    // O que: busca com query em branco e ignorada (nao chama o repositorio).
    // Caso de uso: usuario aperta buscar com o campo vazio -> nao fazer requisicao a toa.
    @Test
    fun search_blankQuery_doesNothing() = runTest {
        viewModel.query = "   "

        viewModel.search()

        assertFalse(viewModel.hasSearched)
        coVerify(exactly = 0) { searchRepository.searchUsers(any()) }
    }

    // O que: busca com sucesso preenche results e marca hasSearched.
    // Caso de uso: usuario digita um username e ve a lista de usuarios encontrados.
    @Test
    fun search_success_populatesResults() = runTest {
        coEvery { searchRepository.searchUsers("bob") } returns
            listOf(UserResponse("u2", "Bob", "bob"))
        viewModel.query = "bob"

        viewModel.search()

        assertEquals(1, viewModel.results.size)
        assertEquals("bob", viewModel.results[0].username)
        assertTrue(viewModel.hasSearched)
        assertFalse(viewModel.connectionError)
        assertFalse(viewModel.isLoading)
    }

    // O que: falha na busca liga connectionError e desliga loading.
    // Caso de uso: sem rede ao buscar -> mostrar erro em vez de travar.
    @Test
    fun search_error_setsConnectionError() = runTest {
        coEvery { searchRepository.searchUsers(any()) } throws RuntimeException("boom")
        viewModel.query = "bob"

        viewModel.search()

        assertTrue(viewModel.connectionError)
        assertFalse(viewModel.isLoading)
    }

    // O que: criar conversa com sucesso liga isConversationCreated e chama o repositorio com o id certo.
    // Caso de uso: usuario toca em um resultado da busca para abrir/iniciar a conversa.
    @Test
    fun startConversation_success_setsCreatedFlag() = runTest {
        val user = UserResponse("u2", "Bob", "bob")
        coEvery { searchRepository.createConversation("u2") } returns
            ConversationResponse("c1", user, null)

        viewModel.startConversation(user)

        assertTrue(viewModel.isConversationCreated)
        assertFalse(viewModel.connectionError)
        coVerify { searchRepository.createConversation("u2") }
    }

    // O que: falha ao criar conversa liga connectionError e mantem isConversationCreated falso.
    // Caso de uso: sem rede ao iniciar a conversa -> sinalizar erro e nao navegar.
    @Test
    fun startConversation_error_setsConnectionError() = runTest {
        coEvery { searchRepository.createConversation(any()) } throws RuntimeException("x")

        viewModel.startConversation(UserResponse("u2", "Bob", "bob"))

        assertTrue(viewModel.connectionError)
        assertFalse(viewModel.isConversationCreated)
    }

    // O que: notifyTransition zera o flag de conversa criada.
    // Caso de uso: apos navegar para o chat, evitar reabrir a navegacao.
    @Test
    fun notifyTransition_resetsCreatedFlag() {
        viewModel.isConversationCreated = true

        viewModel.notifyTransition()

        assertFalse(viewModel.isConversationCreated)
    }
}
