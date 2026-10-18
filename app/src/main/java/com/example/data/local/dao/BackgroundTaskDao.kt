package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BackgroundTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackgroundTaskDao {
    @Query("SELECT * FROM background_tasks ORDER BY scheduledEpochMillis ASC")
    fun getAllTasksFlow(): Flow<List<BackgroundTaskEntity>>

    @Query("SELECT * FROM background_tasks WHERE status = 'PENDING' ORDER BY scheduledEpochMillis ASC")
    suspend fun getPendingTasks(): List<BackgroundTaskEntity>

    @Query("SELECT * FROM background_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): BackgroundTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: BackgroundTaskEntity)

    @Update
    suspend fun updateTask(task: BackgroundTaskEntity)

    @Query("UPDATE background_tasks SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String, completedAt: Long? = System.currentTimeMillis())

    @Query("DELETE FROM background_tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM background_tasks WHERE status = 'COMPLETED' OR status = 'CANCELLED'")
    suspend fun clearCompletedTasks()
}
