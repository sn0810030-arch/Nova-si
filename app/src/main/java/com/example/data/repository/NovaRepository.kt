package com.example.data.repository

import com.example.data.api.GeminiClient
import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.model.NovaPersona
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NovaRepository(
    private val chatDao: ChatDao,
    private val preferencesRepository: PreferencesRepository
) {
    fun getAllSessions(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(
        title: String = "New Conversation",
        persona: NovaPersona = NovaPersona.GENERAL
    ): ChatSessionEntity {
        val session = ChatSessionEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            persona = persona.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isPinned = false
        )
        chatDao.insertSession(session)
        return session
    }

    suspend fun getSession(sessionId: String): ChatSessionEntity? {
        return chatDao.getSessionById(sessionId)
    }

    suspend fun updateSessionTitle(sessionId: String, newTitle: String) {
        chatDao.updateSessionTitle(sessionId, newTitle)
    }

    suspend fun togglePinSession(sessionId: String, isPinned: Boolean) {
        chatDao.togglePinSession(sessionId, isPinned)
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun clearAllSessions() {
        chatDao.deleteAllSessions()
    }

    suspend fun addMessage(
        sessionId: String,
        text: String,
        isFromUser: Boolean,
        modelUsed: String? = null,
        isError: Boolean = false
    ): Long {
        val entity = ChatMessageEntity(
            sessionId = sessionId,
            text = text,
            isFromUser = isFromUser,
            timestamp = System.currentTimeMillis(),
            modelUsed = modelUsed,
            isError = isError
        )
        val id = chatDao.insertMessage(entity)
        // If this is the first user message, update session title
        val session = chatDao.getSessionById(sessionId)
        if (session != null && (session.title == "New Conversation" || session.title.isBlank()) && isFromUser) {
            val autoTitle = text.take(32).trim()
            chatDao.updateSessionTitle(sessionId, autoTitle)
        }
        return id
    }

    suspend fun updateMessageText(messageId: Long, text: String, isError: Boolean = false) {
        chatDao.updateMessageText(messageId, text, isError)
    }

    suspend fun deleteMessagesForSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
    }

    suspend fun searchMessages(query: String): List<ChatMessageEntity> {
        return chatDao.searchMessages(query)
    }
}
