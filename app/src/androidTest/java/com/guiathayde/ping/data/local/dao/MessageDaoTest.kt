package com.guiathayde.ping.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guiathayde.ping.data.local.AppDatabase
import com.guiathayde.ping.data.remote.dto.MessageResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MessageDao

    private fun message(id: String, conversationId: String, timestamp: Long, content: String = "c-$id") =
        MessageResponse(
            id = id,
            conversationId = conversationId,
            senderId = "sender",
            content = content,
            timestamp = timestamp,
            status = "sent"
        )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.messageDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // O que: observeMessages devolve as mensagens em ordem cronologica (timestamp ASC).
    // Caso de uso: o chat mostra as mensagens da mais antiga para a mais nova, de cima para baixo.
    @Test
    fun observeMessages_ordersByTimestampAsc() = runTest {
        dao.upsertAll(
            listOf(
                message("m3", "c1", 30),
                message("m1", "c1", 10),
                message("m2", "c1", 20)
            )
        )

        val list = dao.observeMessages("c1").first()

        assertEquals(listOf("m1", "m2", "m3"), list.map { it.id })
    }

    // O que: observeMessages traz so as mensagens da conversa pedida (filtro por conversationId).
    // Caso de uso: abrir uma conversa nao pode vazar mensagens de outra conversa.
    @Test
    fun observeMessages_filtersByConversation() = runTest {
        dao.upsertAll(
            listOf(
                message("m1", "c1", 10),
                message("m2", "c2", 20),
                message("m3", "c1", 30)
            )
        )

        val list = dao.observeMessages("c1").first()

        assertEquals(listOf("m1", "m3"), list.map { it.id })
    }

    // O que: upsert com id existente substitui a mensagem (nao duplica).
    // Caso de uso: a mesma mensagem chegando por REST e por WebSocket deve aparecer uma unica vez.
    @Test
    fun upsert_withSameId_replacesRow() = runTest {
        dao.upsert(message("m1", "c1", 10, "first"))
        dao.upsert(message("m1", "c1", 10, "edited"))

        val list = dao.observeMessages("c1").first()

        assertEquals(1, list.size)
        assertEquals("edited", list[0].content)
    }

    // O que: observeMessages de uma conversa sem mensagens retorna lista vazia.
    // Caso de uso: conversa recem-criada abre sem mensagens, sem quebrar a tela.
    @Test
    fun observeMessages_noMessages_returnsEmpty() = runTest {
        val list = dao.observeMessages("none").first()

        assertTrue(list.isEmpty())
    }
}
