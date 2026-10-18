package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "correction_rules",
    indices = [Index(value = ["taskType"])]
)
data class CorrectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskType: String, // "whatsapp_message", "sms_message", "email", "calendar_event", "alarm_set", "general_response"
    val rule: String,
    val forbiddenBehavior: String,
    val originalRequest: String,
    val incorrectOutput: String,
    val userCorrection: String,
    val correctedByUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
