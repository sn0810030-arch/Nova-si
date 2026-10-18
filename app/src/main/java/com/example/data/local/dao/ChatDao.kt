package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_sessions ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("UPDATE chat_sessions SET title = :newTitle, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, newTitle: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_sessions SET isPinned = :isPinned WHERE id = :sessionId")
    suspend fun togglePinSession(sessionId: String, isPinned: Boolean)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageForSession(sessionId: String): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("UPDATE chat_messages SET text = :text, isError = :isError WHERE id = :messageId")
    suspend fun updateMessageText(messageId: Long, text: String, isError: Boolean)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(query: String): List<ChatMessageEntity>
}
