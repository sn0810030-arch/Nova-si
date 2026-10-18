package com.example

import com.example.data.api.GeminiClient
import com.example.data.model.NovaPersona
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testNovaPersonasAreDefined() {
        assertEquals(5, NovaPersona.values().size)
        assertNotNull(NovaPersona.GENERAL)
        assertNotNull(NovaPersona.CODE)
        assertNotNull(NovaPersona.WRITING)
        assertNotNull(NovaPersona.TUTOR)
        assertNotNull(NovaPersona.CREATIVE)
    }

    @Test
    fun testOfflineFallbackProvidesContent() {
        val response = GeminiClient.generateLocalFallbackResponse("Hello Nova", NovaPersona.GENERAL.name)
        assertTrue(response.isNotEmpty())
        assertTrue(response.contains("Nova AI"))
    }

    @Test
    fun testVoiceCommandsClassification() {
        val codeCommand = "switch to code mode"
        assertTrue(codeCommand.contains("code mode") || codeCommand.contains("switch to code"))

        val newChatCommand = "start new chat"
        assertTrue(newChatCommand.contains("new chat") || newChatCommand.contains("new conversation"))

        val muteCommand = "stop speaking"
        assertTrue(muteCommand.contains("stop speaking") || muteCommand.contains("mute"))
    }
}
