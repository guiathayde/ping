package com.guiathayde.ping.ui.auth

import com.guiathayde.ping.data.repository.AuthRepository
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

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val viewModel = AuthViewModel(authRepository)

    // O que: campos vazios marcam fieldsError e nem chamam o repositorio.
    // Caso de uso: usuario aperta "Entrar" sem preencher nome/username -> mostrar erro de validacao.
    @Test
    fun performLogin_withEmptyFields_setsFieldsErrorAndDoesNotCallRepo() {
        viewModel.displayName = "   "
        viewModel.username = ""

        viewModel.performLogin()

        assertTrue(viewModel.fieldsError)
        assertFalse(viewModel.isLoginSuccessful)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    // O que: login bem-sucedido liga isLoginSuccessful e desliga loading/erro.
    // Caso de uso: credenciais validas -> navegar para a lista de conversas.
    @Test
    fun performLogin_success_setsLoginSuccessful() = runTest {
        coEvery { authRepository.login("Alice", "alice") } returns "success"
        viewModel.displayName = "Alice"
        viewModel.username = "alice"

        viewModel.performLogin()

        assertTrue(viewModel.isLoginSuccessful)
        assertFalse(viewModel.connectionError)
        assertFalse(viewModel.isLoading)
    }

    // O que: espacos em volta de nome/username sao removidos antes de logar.
    // Caso de uso: usuario digita " Alice " com espaco; deve logar como "Alice".
    @Test
    fun performLogin_trimsWhitespaceBeforeLogin() = runTest {
        coEvery { authRepository.login("Alice", "alice") } returns "success"
        viewModel.displayName = "  Alice  "
        viewModel.username = " alice "

        viewModel.performLogin()

        assertEquals("Alice", viewModel.displayName)
        assertEquals("alice", viewModel.username)
        assertTrue(viewModel.isLoginSuccessful)
    }

    // O que: falha de login liga connectionError e mantem isLoginSuccessful falso.
    // Caso de uso: servidor fora do ar / sem rede -> mostrar mensagem de erro de conexao.
    @Test
    fun performLogin_failure_setsConnectionError() = runTest {
        coEvery { authRepository.login(any(), any()) } returns "failed_connection"
        viewModel.displayName = "Alice"
        viewModel.username = "alice"

        viewModel.performLogin()

        assertTrue(viewModel.connectionError)
        assertFalse(viewModel.isLoginSuccessful)
        assertFalse(viewModel.isLoading)
    }

    // O que: notifyTransition zera o flag de sucesso.
    // Caso de uso: apos navegar para a proxima tela, evitar disparar a navegacao de novo.
    @Test
    fun notifyTransition_resetsLoginSuccessful() {
        viewModel.isLoginSuccessful = true

        viewModel.notifyTransition()

        assertFalse(viewModel.isLoginSuccessful)
    }
}
