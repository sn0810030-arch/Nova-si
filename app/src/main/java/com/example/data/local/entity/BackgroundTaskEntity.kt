package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "background_tasks",
    indices = [
        Index(value = ["status"]),
        Index(value = ["scheduledEpochMillis"])
    ]
)
data class BackgroundTaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val taskType: String, // "REMINDER", "ALARM", "CALENDAR", "MESSAGE_DRAFT"
    val status: String = "PENDING", // "PENDING", "RUNNING", "COMPLETED", "FAILED", "CANCELLED"
    val scheduledEpochMillis: Long,
    val payload: String = "{}",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
