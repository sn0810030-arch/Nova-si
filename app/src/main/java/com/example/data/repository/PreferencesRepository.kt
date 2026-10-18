package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.GeminiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_ai_prefs", Context.MODE_PRIVATE)

    private val _apiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow(
        prefs.getString("selected_model", GeminiClient.DEFAULT_MODEL) ?: GeminiClient.DEFAULT_MODEL
    )
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _temperature = MutableStateFlow(prefs.getFloat("temperature", 0.7f))
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _ttsEnabled = MutableStateFlow(prefs.getBoolean("tts_enabled", true))
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    fun setApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key.trim()).apply()
        _apiKey.value = key.trim()
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString("selected_model", model).apply()
        _selectedModel.value = model
    }

    fun setTemperature(temp: Float) {
        prefs.edit().putFloat("temperature", temp).apply()
        _temperature.value = temp
    }

    fun setTtsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("tts_enabled", enabled).apply()
        _ttsEnabled.value = enabled
    }
}
