package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "memory_records",
    indices = [
        Index(value = ["category"]),
        Index(value = ["key"])
    ]
)
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "PREFERENCE", "LONG_TERM_FACT", "CONVERSATION_SUMMARY", "MISTAKE_CORRECTION"
    val key: String,
    val content: String,
    val source: String = "USER_EXPLICIT",
    val importance: Int = 5,
    val timestamp: Long = System.currentTimeMillis(),
    val verified: Boolean = true
)
