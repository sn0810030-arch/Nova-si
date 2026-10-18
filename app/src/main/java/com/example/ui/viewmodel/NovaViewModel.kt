package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.local.NovaDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.model.NovaPersona
import com.example.data.repository.NovaRepository
import com.example.data.repository.PreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NovaDatabase.getInstance(application)
    private val preferencesRepository = PreferencesRepository(application)
    private val repository = NovaRepository(database.chatDao(), preferencesRepository)

    // Current Session ID
    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    // Current Persona
    private val _currentPersona = MutableStateFlow(NovaPersona.GENERAL)
    val currentPersona: StateFlow<NovaPersona> = _currentPersona.asStateFlow()

    // Active session details
    private val _currentSession = MutableStateFlow<ChatSessionEntity?>(null)
    val currentSession: StateFlow<ChatSessionEntity?> = _currentSession.asStateFlow()

    // Messages for current session
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId != null) {
                repository.getMessagesForSession(sessionId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All sessions
    val allSessions: StateFlow<List<ChatSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query
    val searchQuery = MutableStateFlow("")

    val filteredSessions: StateFlow<List<ChatSessionEntity>> = combine(allSessions, searchQuery) { sessions, query ->
        if (query.isBlank()) sessions else sessions.filter { it.title.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI State
    val isGenerating = MutableStateFlow(false)
    val currentStreamedChunk = MutableStateFlow("")

    // Settings
    val apiKey: StateFlow<String> = preferencesRepository.apiKey
    val selectedModel: StateFlow<String> = preferencesRepository.selectedModel
    val temperature: StateFlow<Float> = preferencesRepository.temperature
    val ttsEnabled: StateFlow<Boolean> = preferencesRepository.ttsEnabled

    val connectionTestResult = MutableStateFlow<String?>(null)
    val isTestingConnection = MutableStateFlow(false)

    // Text to Speech
    private var tts: TextToSpeech? = null
    val isSpeaking = MutableStateFlow(false)

    private var activeGenerationJob: Job? = null

    init {
        initTts(application)
        loadInitialSession()
    }

    private fun initTts(application: Application) {
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    private fun loadInitialSession() {
        viewModelScope.launch {
            val sessions = database.chatDao().getAllSessions()
            // We collect first value or create default
            launch {
                repository.getAllSessions().collect { list ->
                    if (_currentSessionId.value == null) {
                        if (list.isNotEmpty()) {
                            selectSession(list.first().id)
                        } else {
                            createNewSession()
                        }
                    }
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        viewModelScope.launch {
            val session = repository.getSession(sessionId)
            _currentSession.value = session
            if (session != null) {
                val persona = runCatching { NovaPersona.valueOf(session.persona) }.getOrDefault(NovaPersona.GENERAL)
                _currentPersona.value = persona
            }
        }
    }

    fun createNewSession(persona: NovaPersona = _currentPersona.value) {
        viewModelScope.launch {
            val newSession = repository.createNewSession(
                title = "New Conversation",
                persona = persona
            )
            _currentPersona.value = persona
            _currentSessionId.value = newSession.id
            _currentSession.value = newSession
        }
    }

    fun setPersona(persona: NovaPersona) {
        _currentPersona.value = persona
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch {
            val session = repository.getSession(sessionId)
            if (session != null) {
                val updated = session.copy(persona = persona.name)
                database.chatDao().updateSession(updated)
                _currentSession.value = updated
            }
        }
    }

    // Voice Command State
    val autoReadVoiceResponse = MutableStateFlow(true)
    val lastVoiceCommandText = MutableStateFlow("")
    val isVoiceListening = MutableStateFlow(false)

    fun sendMessage(userText: String, autoSpeak: Boolean = false) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || isGenerating.value) return

        val sessionId = _currentSessionId.value ?: return

        activeGenerationJob?.cancel()
        activeGenerationJob = viewModelScope.launch {
            isGenerating.value = true
            var finalGeneratedText = ""

            // 1. Insert user message to Room
            repository.addMessage(
                sessionId = sessionId,
                text = trimmed,
                isFromUser = true
            )

            // 2. Fetch conversation history for context
            val history = currentMessages.value.map { it.text to it.isFromUser }.takeLast(10)
            val effectiveKey = GeminiClient.getEffectiveApiKey(apiKey.value)
            val model = selectedModel.value
            val persona = _currentPersona.value

            if (effectiveKey != null) {
                // Call live Gemini Streaming API
                var assistantMessageId: Long? = null
                val fullResponse = StringBuilder()

                try {
                    // Pre-create placeholder message
                    assistantMessageId = repository.addMessage(
                        sessionId = sessionId,
                        text = "",
                        isFromUser = false,
                        modelUsed = model
                    )

                    GeminiClient.streamGenerateContent(
                        apiKey = effectiveKey,
                        model = model,
                        messages = history + listOf(trimmed to true),
                        systemPrompt = persona.systemPrompt,
                        temperature = temperature.value
                    ).collect { chunk ->
                        fullResponse.append(chunk)
                        repository.updateMessageText(assistantMessageId, fullResponse.toString())
                    }

                    finalGeneratedText = fullResponse.toString()
                    if (finalGeneratedText.isBlank()) {
                        finalGeneratedText = "No response generated by model."
                        repository.updateMessageText(assistantMessageId, finalGeneratedText)
                    }
                } catch (e: Exception) {
                    Log.e("NovaViewModel", "Streaming failed: ${e.message}", e)
                    // Try single call fallback
                    val singleResult = GeminiClient.generateContentSingle(
                        apiKey = effectiveKey,
                        model = model,
                        messages = history + listOf(trimmed to true),
                        systemPrompt = persona.systemPrompt,
                        temperature = temperature.value
                    )
                    singleResult.onSuccess { text ->
                        finalGeneratedText = text
                        if (assistantMessageId != null) {
                            repository.updateMessageText(assistantMessageId, text)
                        } else {
                            repository.addMessage(sessionId, text, false, model)
                        }
                    }.onFailure { ex ->
                        val errorText = "Unable to reach Gemini API (${ex.message}). Falling back to Nova offline mode.\n\n" +
                                GeminiClient.generateLocalFallbackResponse(trimmed, persona.name)
                        finalGeneratedText = errorText
                        if (assistantMessageId != null) {
                            repository.updateMessageText(assistantMessageId, errorText)
                        } else {
                            repository.addMessage(sessionId, errorText, false, "Nova Core Offline")
                        }
                    }
                }
            } else {
                // Graceful Offline Intelligent Engine
                val offlineText = GeminiClient.generateLocalFallbackResponse(trimmed, persona.name)
                finalGeneratedText = offlineText
                repository.addMessage(
                    sessionId = sessionId,
                    text = offlineText,
                    isFromUser = false,
                    modelUsed = "Nova Core"
                )
            }

            isGenerating.value = false

            if (autoSpeak && finalGeneratedText.isNotBlank()) {
                speakText(finalGeneratedText)
            }
        }
    }

    fun processVoiceCommand(spokenText: String, onFeedback: (String) -> Unit) {
        val trimmed = spokenText.trim()
        if (trimmed.isEmpty()) return
        lastVoiceCommandText.value = trimmed
        val clean = trimmed.lowercase()

        when {
            clean.contains("new chat") || clean.contains("new conversation") || clean.contains("create chat") -> {
                createNewSession()
                onFeedback("Created new conversation")
                if (autoReadVoiceResponse.value) {
                    speakText("Started a new conversation. How can I help you?")
                }
            }
            clean.contains("clear chat") || clean.contains("clear messages") || clean.contains("delete chat") -> {
                clearCurrentChat()
                onFeedback("Cleared chat conversation")
                if (autoReadVoiceResponse.value) {
                    speakText("Conversation cleared.")
                }
            }
            clean.contains("code mode") || clean.contains("switch to code") -> {
                setPersona(NovaPersona.CODE)
                onFeedback("Switched to Code Architect mode")
                if (autoReadVoiceResponse.value) {
                    speakText("Switched to Code Architect mode. Ready for programming questions.")
                }
            }
            clean.contains("writing mode") || clean.contains("switch to writing") || clean.contains("wordsmith") -> {
                setPersona(NovaPersona.WRITING)
                onFeedback("Switched to Wordsmith mode")
                if (autoReadVoiceResponse.value) {
                    speakText("Switched to Wordsmith mode. Ready to write and edit.")
                }
            }
            clean.contains("tutor mode") || clean.contains("switch to tutor") || clean.contains("learning mode") -> {
                setPersona(NovaPersona.TUTOR)
                onFeedback("Switched to Socratic Tutor mode")
                if (autoReadVoiceResponse.value) {
                    speakText("Switched to Socratic Tutor mode.")
                }
            }
            clean.contains("creative mode") || clean.contains("switch to creative") || clean.contains("muse mode") -> {
                setPersona(NovaPersona.CREATIVE)
                onFeedback("Switched to Creative Muse mode")
                if (autoReadVoiceResponse.value) {
                    speakText("Switched to Creative Muse mode.")
                }
            }
            clean.contains("core mode") || clean.contains("general mode") || clean.contains("switch to general") -> {
                setPersona(NovaPersona.GENERAL)
                onFeedback("Switched to Nova Core mode")
                if (autoReadVoiceResponse.value) {
                    speakText("Switched to Nova Core mode.")
                }
            }
            clean.contains("stop speaking") || clean.contains("mute") || clean.contains("shut up") || clean.contains("silence") -> {
                stopSpeaking()
                onFeedback("Speech muted")
            }
            clean.contains("read aloud") || clean.contains("read last") || clean.contains("read response") -> {
                val lastAssistantMsg = currentMessages.value.lastOrNull { !it.isFromUser }?.text
                if (!lastAssistantMsg.isNullOrBlank()) {
                    speakText(lastAssistantMsg)
                    onFeedback("Reading response aloud")
                } else {
                    onFeedback("No previous response to read")
                }
            }
            else -> {
                // Execute prompt with Nova AI
                onFeedback("Processing voice prompt…")
                sendMessage(trimmed, autoSpeak = autoReadVoiceResponse.value)
            }
        }
    }

    fun retryLastMessage() {
        val messages = currentMessages.value
        val lastUserMessage = messages.lastOrNull { it.isFromUser }?.text ?: return
        sendMessage(lastUserMessage)
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                val remaining = allSessions.value.filter { it.id != sessionId }
                if (remaining.isNotEmpty()) {
                    selectSession(remaining.first().id)
                } else {
                    createNewSession()
                }
            }
        }
    }

    fun togglePinSession(sessionId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinSession(sessionId, !isPinned)
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updateSessionTitle(sessionId, trimmed)
            if (_currentSessionId.value == sessionId) {
                _currentSession.value = _currentSession.value?.copy(title = trimmed)
            }
        }
    }

    fun clearCurrentChat() {
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch {
            repository.deleteMessagesForSession(sessionId)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllSessions()
            createNewSession()
        }
    }

    fun saveApiKey(key: String) {
        preferencesRepository.setApiKey(key)
    }

    fun setModel(model: String) {
        preferencesRepository.setSelectedModel(model)
    }

    fun setTemperature(temp: Float) {
        preferencesRepository.setTemperature(temp)
    }

    fun testConnection() {
        val key = GeminiClient.getEffectiveApiKey(apiKey.value)
        if (key == null) {
            connectionTestResult.value = "Error: No API Key found. Please enter a valid Gemini API key."
            return
        }

        viewModelScope.launch {
            isTestingConnection.value = true
            connectionTestResult.value = null
            val result = GeminiClient.testConnection(key, selectedModel.value)
            result.onSuccess { msg ->
                connectionTestResult.value = "Success: $msg"
            }.onFailure { err ->
                connectionTestResult.value = "Error: ${err.message}"
            }
            isTestingConnection.value = false
        }
    }

    fun speakText(text: String) {
        val clean = text.replace(Regex("```[\\s\\S]*?```"), "Code snippet omitted.")
            .replace(Regex("[*#_`>]"), "")
        tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "nova_tts")
        isSpeaking.value = true
    }

    fun stopSpeaking() {
        tts?.stop()
        isSpeaking.value = false
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        activeGenerationJob?.cancel()
    }
}
