package com.example.avatar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AvatarState(val displayName: String, val description: String) {
    IDLE("Idle", "Subtle breathing and ambient micro-movements"),
    LISTENING("Listening", "Focused attentive posture with glowing cyan audio aura"),
    THINKING("Thinking", "Thoughtful upward gaze and pulsating cyber nodes"),
    SPEAKING("Speaking", "Expressive head gestures with synchronized mouth phonemes"),
    HAPPY("Happy", "Warm smiling expression with upbeat head nod"),
    SURPRISED("Surprised", "Widened eyes, lifted brows, and dynamic posture"),
    CONFUSED("Confused", "Slight head tilt with quizzical brow furrow"),
    SAD("Sad", "Gentle downward gaze with subdued posture"),
    APOLOGETIC("Apologetic", "Humble remorseful expression acknowledging error"),
    TASK_SUCCESS("Success", "Joyful celebration gesture with bright cyan pulse"),
    TASK_FAILED("Failed", "Concerned apologetic gesture with alert glow")
}

object AvatarStateManager {
    private val _currentState = MutableStateFlow(AvatarState.IDLE)
    val currentState: StateFlow<AvatarState> = _currentState.asStateFlow()

    private val _speechAmplitude = MutableStateFlow(0f)
    val speechAmplitude: StateFlow<Float> = _speechAmplitude.asStateFlow()

    fun transitionTo(newState: AvatarState) {
        _currentState.value = newState
    }

    fun setSpeechAmplitude(amplitude: Float) {
        _speechAmplitude.value = amplitude.coerceIn(0f, 1f)
    }

    fun onUserStartedSpeaking() {
        transitionTo(AvatarState.LISTENING)
    }

    fun onProcessingRequest() {
        transitionTo(AvatarState.THINKING)
    }

    fun onSpeakingResponse() {
        transitionTo(AvatarState.SPEAKING)
    }

    fun onApologizingForMistake() {
        transitionTo(AvatarState.APOLOGETIC)
    }

    fun onTaskCompleted(success: Boolean) {
        transitionTo(if (success) AvatarState.TASK_SUCCESS else AvatarState.TASK_FAILED)
    }

    fun returnToIdle() {
        _currentState.value = AvatarState.IDLE
        _speechAmplitude.value = 0f
    }
}
