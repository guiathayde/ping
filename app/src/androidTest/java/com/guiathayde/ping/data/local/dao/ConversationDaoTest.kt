package com.guiathayde.ping.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.guiathayde.ping.data.local.AppDatabase
import com.guiathayde.ping.data.local.entity.Conversation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ConversationDao

    private fun conversation(id: String, timestamp: Long?, content: String? = "msg-$id") =
        Conversation(
            id = id,
            participantId = "p-$id",
            participantDisplayName = "Name $id",
            participantUsername = "user$id",
            lastMessageContent = content,
            lastMessageTimestamp = timestamp
        )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.conversationDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // O que: upsert grava a conversa e getById a recupera pelo id.
    // Caso de uso: persistir a conversa vinda do servidor e le-la depois pelo id.
    @Test
    fun upsert_thenGetById_returnsConversation() = runTest {
        dao.upsert(conversation("c1", 100))

        val loaded = dao.getById("c1")

        assertNotNull(loaded)
        assertEquals("Name c1", loaded?.participantDisplayName)
        assertEquals("userc1", loaded?.participantUsername)
    }

    // O que: getById com id inexistente retorna null.
    // Caso de uso: checar se uma conversa ja existe no cache antes de cria-la.
    @Test
    fun getById_unknownId_returnsNull() = runTest {
        assertNull(dao.getById("missing"))
    }

    // O que: observeConversations ordena pela ultima mensagem (timestamp DESC).
    // Caso de uso: a lista de conversas mostra no topo quem mandou mensagem mais recente.
    @Test
    fun observeConversations_ordersByLastMessageTimestampDesc() = runTest {
        dao.upsertAll(
            listOf(
                conversation("old", 100),
                conversation("new", 300),
                conversation("mid", 200)
            )
        )

        val list = dao.observeConversations().first()

        assertEquals(listOf("new", "mid", "old"), list.map { it.id })
    }

    // O que: upsert com id existente substitui a linha (nao duplica).
    // Caso de uso: re-sincronizar uma conversa ja salva atualiza os dados em vez de criar outra.
    @Test
    fun upsert_withSameId_replacesRow() = runTest {
        dao.upsert(conversation("c1", 100, "first"))
        dao.upsert(conversation("c1", 150, "second"))

        val loaded = dao.getById("c1")
        assertEquals("second", loaded?.lastMessageContent)
        assertEquals(150L, loaded?.lastMessageTimestamp)
        assertEquals(1, dao.observeConversations().first().size)
    }

    // O que: updateLastMessage aplica a mensagem quando ela e mais nova que a atual.
    // Caso de uso: chegou mensagem nova -> atualizar o preview e a ordenacao da lista.
    @Test
    fun updateLastMessage_appliesNewerMessage() = runTest {
        dao.upsert(conversation("c1", 100, "old"))

        dao.updateLastMessage("c1", "newer", 200)

        val loaded = dao.getById("c1")
        assertEquals("newer", loaded?.lastMessageContent)
        assertEquals(200L, loaded?.lastMessageTimestamp)
    }

    // O que: updateLastMessage ignora mensagem mais antiga que a atual (guarda por timestamp).
    // Caso de uso: mensagem antiga chegando fora de ordem nao deve sobrescrever o preview mais recente.
    @Test
    fun updateLastMessage_ignoresOlderMessage() = runTest {
        dao.upsert(conversation("c1", 200, "current"))

        dao.updateLastMessage("c1", "stale", 100)

        val loaded = dao.getById("c1")
        assertEquals("current", loaded?.lastMessageContent)
        assertEquals(200L, loaded?.lastMessageTimestamp)
    }

    // O que: updateLastMessage aplica quando a conversa ainda nao tinha timestamp (null).
    // Caso de uso: primeira mensagem de uma conversa recem-criada (sem last_message) preenche o preview.
    @Test
    fun updateLastMessage_appliesWhenTimestampWasNull() = runTest {
        dao.upsert(conversation("c1", null, null))

        dao.updateLastMessage("c1", "first", 50)

        val loaded = dao.getById("c1")
        assertEquals("first", loaded?.lastMessageContent)
        assertEquals(50L, loaded?.lastMessageTimestamp)
    }
}
