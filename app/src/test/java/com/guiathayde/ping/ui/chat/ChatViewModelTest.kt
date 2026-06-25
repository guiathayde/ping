package com.guiathayde.ping.ui.chat

import com.guiathayde.ping.data.remote.dto.MessageResponse
import com.guiathayde.ping.data.repository.ChatRepository
import com.guiathayde.ping.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val chatRepository: ChatRepository = mockk(relaxed = true)

    private fun message(id: String, ts: Long) =
        MessageResponse(id, "c1", "u1", "content-$id", ts, "sent")

    // O que: loadMessages coleta as mensagens do Room para o estado e dispara o refresh remoto.
    // Caso de uso: ao abrir a conversa, mostrar o historico em cache e sincronizar com o servidor.
    @Test
    fun loadMessages_observesCachedMessagesAndRefreshes() = runTest {
        every { chatRepository.observeMessages("c1") } returns
            flowOf(listOf(message("m1", 1), message("m2", 2)))
        coEvery { chatRepository.refreshMessages("c1") } returns Unit
        val viewModel = ChatViewModel(chatRepository)

        viewModel.loadMessages("c1")

        assertEquals(2, viewModel.messages.size)
        assertEquals("m1", viewModel.messages[0].id)
        assertFalse(viewModel.isLoading)
        assertFalse(viewModel.connectionError)
        coVerify { chatRepository.refreshMessages("c1") }
    }

    // O que: falha no refresh liga connectionError e desliga loading.
    // Caso de uso: sem rede ao abrir a conversa -> sinalizar erro (cache, se houver, segue visivel).
    @Test
    fun loadMessages_refreshError_setsConnectionError() = runTest {
        every { chatRepository.observeMessages(any()) } returns flowOf(emptyList())
        coEvery { chatRepository.refreshMessages(any()) } throws RuntimeException("net")
        val viewModel = ChatViewModel(chatRepository)

        viewModel.loadMessages("c1")

        assertTrue(viewModel.connectionError)
        assertFalse(viewModel.isLoading)
    }

    // O que: enviar mensagem em branco (so espacos) nao chama o repositorio.
    // Caso de uso: usuario aperta enviar com a caixa vazia -> nao mandar mensagem vazia.
    @Test
    fun sendMessage_blankContent_doesNotSend() = runTest {
        every { chatRepository.observeMessages(any()) } returns flowOf(emptyList())
        val viewModel = ChatViewModel(chatRepository)
        viewModel.loadMessages("c1")
        viewModel.messageText = "   "

        viewModel.sendMessage()

        coVerify(exactly = 0) { chatRepository.sendMessage(any(), any()) }
    }

    // O que: enviar mensagem valida limpa o campo de texto e delega ao repositorio.
    // Caso de uso: usuario digita e envia -> a caixa esvazia e a mensagem vai para o backend/Room.
    @Test
    fun sendMessage_clearsTextAndDelegates() = runTest {
        every { chatRepository.observeMessages(any()) } returns flowOf(emptyList())
        coEvery { chatRepository.sendMessage("c1", "hello") } returns Unit
        val viewModel = ChatViewModel(chatRepository)
        viewModel.loadMessages("c1")
        viewModel.messageText = "hello"

        viewModel.sendMessage()

        assertEquals("", viewModel.messageText)
        coVerify { chatRepository.sendMessage("c1", "hello") }
    }

    // O que: myUserId da ViewModel repassa o id do repositorio.
    // Caso de uso: a tela usa esse id para alinhar as bolhas (minhas mensagens vs do outro).
    @Test
    fun myUserId_delegatesToRepository() {
        every { chatRepository.observeMessages(any()) } returns flowOf(emptyList())
        every { chatRepository.myUserId } returns "u1"
        val viewModel = ChatViewModel(chatRepository)

        assertEquals("u1", viewModel.myUserId)
    }
}
