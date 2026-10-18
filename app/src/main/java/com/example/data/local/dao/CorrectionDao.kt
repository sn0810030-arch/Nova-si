package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CorrectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CorrectionDao {
    @Query("SELECT * FROM correction_rules ORDER BY timestamp DESC")
    fun getAllCorrectionsFlow(): Flow<List<CorrectionEntity>>

    @Query("SELECT * FROM correction_rules ORDER BY timestamp DESC")
    suspend fun getAllCorrections(): List<CorrectionEntity>

    @Query("SELECT * FROM correction_rules WHERE taskType = :taskType ORDER BY timestamp DESC")
    suspend fun getCorrectionsForTask(taskType: String): List<CorrectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrection(correction: CorrectionEntity): Long

    @Query("DELETE FROM correction_rules WHERE id = :id")
    suspend fun deleteCorrection(id: Long)

    @Query("DELETE FROM correction_rules")
    suspend fun clearAllCorrections()
}
