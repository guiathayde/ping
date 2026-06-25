package com.guiathayde.ping.data.remote

import com.guiathayde.ping.data.remote.dto.CreateConversationRequest
import com.guiathayde.ping.data.remote.dto.LoginRequest
import com.guiathayde.ping.data.remote.dto.SendMessageRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

class ApiServiceTest {

    private val api: ApiService = RetrofitInstance.api

    // Sufixo unico para isolar os dados de cada teste no banco.
    private fun unique() = UUID.randomUUID().toString().substring(0, 8)

    private fun bearer(token: String) = "Bearer $token"

    @Before
    fun requireServerRunning() {
        val reachable = try {
            Socket().use { it.connect(InetSocketAddress("localhost", 3000), 1000) }
            true
        } catch (e: Exception) {
            false
        }
        Assume.assumeTrue("Backend nao esta rodando em localhost:3000", reachable)
    }

    // O que: POST /auth/login cria o usuario (se novo) e devolve token + dados do usuario.
    // Caso de uso: primeiro acesso na tela de login -> conta criada e JWT recebido.
    @Test
    fun login_createsUserAndReturnsToken() = runBlocking {
        val username = "alice_${unique()}"

        val response = api.login(LoginRequest("Alice", username))

        assertTrue(response.token.isNotBlank())
        assertTrue(response.user.id.isNotBlank())
        assertEquals("Alice", response.user.displayName)
        assertEquals(username, response.user.username)
    }

    // O que: logar com um username ja existente devolve o MESMO usuario (idempotente).
    // Caso de uso: usuario recorrente entra de novo e cai na mesma conta, nao em uma nova.
    @Test
    fun login_existingUsername_returnsSameUser() = runBlocking {
        val username = "alice_${unique()}"

        val first = api.login(LoginRequest("Alice", username))
        val second = api.login(LoginRequest("Alice", username))

        assertEquals(first.user.id, second.user.id)
    }

    // O que: GET /users/search acha outro usuario por trecho do username e exclui o proprio.
    // Caso de uso: na busca, o usuario encontra contatos mas nunca aparece a si mesmo na lista.
    @Test
    fun searchUsers_findsOtherUserExcludingSelf() = runBlocking {
        val suffix = unique()
        val alice = api.login(LoginRequest("Alice", "alice_$suffix"))
        val bob = api.login(LoginRequest("Bob", "bob_$suffix"))

        val results = api.searchUsers(bearer(alice.token), suffix)

        assertTrue(results.any { it.id == bob.user.id })
        assertFalse(results.any { it.id == alice.user.id })
    }

    // O que: POST /conversations cria a conversa 1:1 e retorna o outro participante.
    // Caso de uso: usuario toca em um resultado da busca e abre/inicia a conversa com ele.
    @Test
    fun createConversation_returnsConversationWithParticipant() = runBlocking {
        val suffix = unique()
        val alice = api.login(LoginRequest("Alice", "alice_$suffix"))
        val bob = api.login(LoginRequest("Bob", "bob_$suffix"))

        val conversation = api.createConversation(
            bearer(alice.token),
            CreateConversationRequest(bob.user.id)
        )

        assertTrue(conversation.id.isNotBlank())
        assertEquals(bob.user.id, conversation.participant?.id)
        assertEquals(bob.user.username, conversation.participant?.username)
        assertNull(conversation.lastMessage)
    }

    // O que: criar a conversa duas vezes entre os mesmos usuarios devolve a MESMA conversa.
    // Caso de uso: reabrir o chat de um contato existente nao deve duplicar conversas.
    @Test
    fun createConversation_isIdempotent() = runBlocking {
        val suffix = unique()
        val alice = api.login(LoginRequest("Alice", "alice_$suffix"))
        val bob = api.login(LoginRequest("Bob", "bob_$suffix"))

        val first = api.createConversation(bearer(alice.token), CreateConversationRequest(bob.user.id))
        val second = api.createConversation(bearer(alice.token), CreateConversationRequest(bob.user.id))

        assertEquals(first.id, second.id)
    }

    // O que: GET /conversations lista as conversas do usuario, incluindo a recem-criada.
    // Caso de uso: ao abrir a tela inicial, a conversa nova aparece na lista.
    @Test
    fun getConversations_includesCreatedConversation() = runBlocking {
        val suffix = unique()
        val alice = api.login(LoginRequest("Alice", "alice_$suffix"))
        val bob = api.login(LoginRequest("Bob", "bob_$suffix"))
        val created = api.createConversation(bearer(alice.token), CreateConversationRequest(bob.user.id))

        val conversations = api.getConversations(bearer(alice.token))

        val match = conversations.find { it.id == created.id }
        assertNotNull(match)
        assertEquals(bob.user.id, match?.participant?.id)
    }

    // O que: POST /conversations/{id}/messages persiste a mensagem e devolve seus dados.
    // Caso de uso: usuario digita e envia uma mensagem dentro da conversa.
    @Test
    fun sendMessage_returnsCreatedMessage() = runBlocking {
        val suffix = unique()
        val alice = api.login(LoginRequest("Alice", "alice_$suffix"))
        val bob = api.login(LoginRequest("Bob", "bob_$suffix"))
        val conversation = api.createConversation(bearer(alice.token), CreateConversationRequest(bob.user.id))

        val content = "ola-$suffix"
        val message = api.sendMessage(bearer(alice.token), conversation.id, SendMessageRequest(content))

        assertTrue(message.id.isNotBlank())
        assertEquals(conversation.id, message.conversationId)
        assertEquals(alice.user.id, message.senderId)
        assertEquals(content, message.content)
        assertEquals("sent", message.status)
    }

    // O que: GET /conversations/{id}/messages devolve as mensagens enviadas na conversa.
    // Caso de uso: ao abrir a conversa, carregar o historico de mensagens do servidor.
    @Test
    fun getMessages_returnsSentMessages() = runBlocking {
        val suffix = unique()
        val alice = api.login(LoginRequest("Alice", "alice_$suffix"))
        val bob = api.login(LoginRequest("Bob", "bob_$suffix"))
        val conversation = api.createConversation(bearer(alice.token), CreateConversationRequest(bob.user.id))

        api.sendMessage(bearer(alice.token), conversation.id, SendMessageRequest("first-$suffix"))
        api.sendMessage(bearer(alice.token), conversation.id, SendMessageRequest("second-$suffix"))

        val messages = api.getMessages(bearer(alice.token), conversation.id)
        val contents = messages.map { it.content }

        assertEquals(2, messages.size)
        assertTrue(contents.contains("first-$suffix"))
        assertTrue(contents.contains("second-$suffix"))
    }
}
