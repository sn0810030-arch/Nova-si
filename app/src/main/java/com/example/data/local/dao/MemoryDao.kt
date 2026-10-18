package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_records ORDER BY importance DESC, timestamp DESC")
    fun getAllMemoriesFlow(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memory_records ORDER BY importance DESC, timestamp DESC")
    suspend fun getAllMemories(): List<MemoryEntity>

    @Query("SELECT * FROM memory_records WHERE category = :category ORDER BY importance DESC, timestamp DESC")
    suspend fun getMemoriesByCategory(category: String): List<MemoryEntity>

    @Query("SELECT * FROM memory_records WHERE `key` = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): MemoryEntity?

    @Query("SELECT * FROM memory_records WHERE content LIKE '%' || :query || '%' OR `key` LIKE '%' || :query || '%' ORDER BY importance DESC LIMIT 10")
    suspend fun searchRelevantMemories(query: String): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Query("DELETE FROM memory_records WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM memory_records")
    suspend fun clearAllMemories()

    @Query("SELECT SUM(LENGTH(content) + LENGTH(`key`) + LENGTH(category) + 40) FROM memory_records")
    suspend fun estimateTotalMemoryBytes(): Long?

    @Query("SELECT COUNT(*) FROM memory_records")
    suspend fun getMemoryCount(): Int

    // Evict lowest importance oldest memories when exceeding budget
    @Query("DELETE FROM memory_records WHERE id IN (SELECT id FROM memory_records ORDER BY importance ASC, timestamp ASC LIMIT :count)")
    suspend fun evictOldestLowPriorityMemories(count: Int)
}
