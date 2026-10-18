package com.example.data.memory

import com.example.data.local.dao.CorrectionDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.entity.CorrectionEntity
import com.example.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

class MemoryManager(
    private val memoryDao: MemoryDao,
    private val correctionDao: CorrectionDao
) {
    companion object {
        const val MAX_BUDGET_BYTES = 30L * 1024L * 1024L // 30 Megabytes Budget
        private val SENSITIVE_KEYWORD_REGEX = Regex(
            "(password|api_key|secret|token|credit_card|cvv|pin|passcode)",
            RegexOption.IGNORE_CASE
        )
    }

    fun getAllMemoriesFlow(): Flow<List<MemoryEntity>> = memoryDao.getAllMemoriesFlow()

    fun getAllCorrectionsFlow(): Flow<List<CorrectionEntity>> = correctionDao.getAllCorrectionsFlow()

    suspend fun saveMemory(
        key: String,
        content: String,
        category: String = "LONG_TERM_FACT",
        source: String = "USER_EXPLICIT",
        importance: Int = 5
    ): Result<Long> {
        val trimmedKey = key.trim()
        val trimmedContent = content.trim()

        // Privacy Check: Refuse sensitive secrets
        if (SENSITIVE_KEYWORD_REGEX.containsMatchIn(trimmedKey) || SENSITIVE_KEYWORD_REGEX.containsMatchIn(trimmedContent)) {
            return Result.failure(SecurityException("Sensitive credentials/passwords cannot be stored in conversational memory."))
        }

        // Deduplication: check existing key
        val existing = memoryDao.getMemoryByKey(trimmedKey)
        val entity = if (existing != null) {
            existing.copy(
                content = trimmedContent,
                importance = importance.coerceAtLeast(existing.importance),
                timestamp = System.currentTimeMillis()
            )
        } else {
            MemoryEntity(
                category = category,
                key = trimmedKey,
                content = trimmedContent,
                source = source,
                importance = importance,
                timestamp = System.currentTimeMillis(),
                verified = true
            )
        }

        // Budget Enforcer Check
        enforceStorageBudget()

        return try {
            val id = memoryDao.insertMemory(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCorrectionRule(
        taskType: String,
        rule: String,
        forbiddenBehavior: String,
        originalRequest: String,
        incorrectOutput: String,
        userCorrection: String
    ): Result<Long> {
        val correction = CorrectionEntity(
            taskType = taskType,
            rule = rule,
            forbiddenBehavior = forbiddenBehavior,
            originalRequest = originalRequest,
            incorrectOutput = incorrectOutput,
            userCorrection = userCorrection,
            correctedByUser = true,
            timestamp = System.currentTimeMillis()
        )
        return try {
            val id = correctionDao.insertCorrection(correction)
            // Also store as memory entry for retrieval
            saveMemory(
                key = "correction_$taskType",
                content = "RULE: $rule | FORBIDDEN: $forbiddenBehavior",
                category = "MISTAKE_CORRECTION",
                source = "SYSTEM_CORRECTION",
                importance = 10
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRelevantContextForPrompt(userPrompt: String): String {
        val keywords = userPrompt.split(" ").filter { it.length > 3 }.take(4)
        val memories = mutableListOf<MemoryEntity>()

        for (word in keywords) {
            memories.addAll(memoryDao.searchRelevantMemories(word))
        }

        // Deduplicate
        val distinctMemories = memories.distinctBy { it.id }.sortedByDescending { it.importance }.take(6)
        if (distinctMemories.isEmpty()) return ""

        val sb = StringBuilder("=== RELEVANT LONG-TERM MEMORY & USER RULES ===\n")
        for (m in distinctMemories) {
            sb.append("- [${m.category}] ${m.key}: ${m.content}\n")
        }
        sb.append("=== END MEMORY ===\n")
        return sb.toString()
    }

    suspend fun getCorrectionsForTask(taskType: String): List<CorrectionEntity> {
        return correctionDao.getCorrectionsForTask(taskType)
    }

    suspend fun getMemoryUsageBytes(): Long {
        return memoryDao.estimateTotalMemoryBytes() ?: 0L
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemory(id)
    }

    suspend fun deleteCorrection(id: Long) {
        correctionDao.deleteCorrection(id)
    }

    suspend fun clearAll() {
        memoryDao.clearAllMemories()
        correctionDao.clearAllCorrections()
    }

    private suspend fun enforceStorageBudget() {
        val currentBytes = getMemoryUsageBytes()
        if (currentBytes > MAX_BUDGET_BYTES) {
            // Evict oldest lowest priority memories
            memoryDao.evictOldestLowPriorityMemories(10)
        }
    }
}
