package com.example

import com.example.action.ActionParser
import com.example.action.ActionValidator
import com.example.action.ParsedAction
import com.example.action.TargetApp
import com.example.avatar.AvatarState
import com.example.avatar.AvatarStateManager
import com.example.data.api.GeminiClient
import com.example.data.local.entity.CorrectionEntity
import com.example.data.model.NovaPersona
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testExactMessagePreservationAndSeparation_Hinglish() {
        val input = "Rahul ko WhatsApp par Hi message karo"
        val action = ActionParser.parse(input)

        assertNotNull("Action should be parsed", action)
        assertTrue("Should be SendMessage action", action is ParsedAction.SendMessage)

        val sendAction = action as ParsedAction.SendMessage
        assertEquals(TargetApp.WHATSAPP, sendAction.app)
        assertEquals("Rahul", sendAction.recipient)
        assertEquals("Hi", sendAction.exactMessage)

        // Strict Requirement: Must fail if recipient is in body or extra text appended
        assertNotEquals("Hi Rahul", sendAction.exactMessage)
        assertFalse(sendAction.exactMessage.contains("how are you"))
    }

    @Test
    fun testExactMessagePreservation_English() {
        val input = "Yesmin, send Rahul a WhatsApp message saying Hi"
        val action = ActionParser.parse(input)

        assertNotNull(action)
        assertTrue(action is ParsedAction.SendMessage)

        val sendAction = action as ParsedAction.SendMessage
        assertEquals(TargetApp.WHATSAPP, sendAction.app)
        assertEquals("Rahul", sendAction.recipient)
        assertEquals("Hi", sendAction.exactMessage)
    }

    @Test
    fun testSmsExactMessageParsing() {
        val input = "Send Mom an SMS message saying I will be late"
        val action = ActionParser.parse(input)

        assertNotNull(action)
        assertTrue(action is ParsedAction.SendMessage)

        val sendAction = action as ParsedAction.SendMessage
        assertEquals(TargetApp.SMS, sendAction.app)
        assertEquals("Mom", sendAction.recipient)
        assertEquals("I will be late", sendAction.exactMessage)
    }

    @Test
    fun testQuotedMessagePreservation() {
        val input = "send John on WhatsApp \"Meeting at 5 PM sharp!\""
        val action = ActionParser.parse(input)

        assertNotNull(action)
        assertTrue(action is ParsedAction.SendMessage)

        val sendAction = action as ParsedAction.SendMessage
        assertEquals("John", sendAction.recipient)
        assertEquals("Meeting at 5 PM sharp!", sendAction.exactMessage)
    }

    @Test
    fun testActionValidatorWithCorrectionRules() {
        val rawAction = ParsedAction.SendMessage(
            app = TargetApp.WHATSAPP,
            recipient = "Rahul",
            exactMessage = "Hi Rahul, Hi",
            originalPrompt = "send Rahul Hi"
        )

        val correctionRules = listOf(
            CorrectionEntity(
                taskType = "whatsapp_message",
                rule = "Send only the exact message explicitly requested",
                forbiddenBehavior = "Do not append greetings, names, explanations, emojis, or extra sentences",
                originalRequest = "Rahul ko WhatsApp par Hi bhejo",
                incorrectOutput = "Hi Rahul",
                userCorrection = "Send only 'Hi'"
            )
        )

        val validation = ActionValidator.validate(rawAction, correctionRules)
        assertTrue(validation.isValid)
        val sanitized = validation.sanitizedAction as ParsedAction.SendMessage
        assertEquals("Hi", sanitized.exactMessage)
        assertNotNull(validation.appliedCorrectionRule)
    }

    @Test
    fun testAlarmParsing() {
        val input = "Set an alarm for 6:30 AM"
        val action = ActionParser.parse(input)

        assertNotNull(action)
        assertTrue(action is ParsedAction.SetAlarm)

        val alarmAction = action as ParsedAction.SetAlarm
        assertEquals(6, alarmAction.hour)
        assertEquals(30, alarmAction.minute)
    }

    @Test
    fun testReminderParsing() {
        val input = "Tomorrow at 8 AM remind me to study"
        val action = ActionParser.parse(input)

        assertNotNull(action)
        assertTrue(action is ParsedAction.SetReminder)

        val reminderAction = action as ParsedAction.SetReminder
        assertEquals("study", reminderAction.title)
        assertTrue(reminderAction.triggerEpochMillis > System.currentTimeMillis())
    }

    @Test
    fun testBluetoothParsing() {
        val input = "Show my connected Bluetooth devices"
        val action = ActionParser.parse(input)

        assertNotNull(action)
        assertTrue(action is ParsedAction.BluetoothControl)
    }

    @Test
    fun testAvatarStateTransitions() {
        AvatarStateManager.returnToIdle()
        assertEquals(AvatarState.IDLE, AvatarStateManager.currentState.value)

        AvatarStateManager.onUserStartedSpeaking()
        assertEquals(AvatarState.LISTENING, AvatarStateManager.currentState.value)

        AvatarStateManager.onProcessingRequest()
        assertEquals(AvatarState.THINKING, AvatarStateManager.currentState.value)

        AvatarStateManager.onSpeakingResponse()
        assertEquals(AvatarState.SPEAKING, AvatarStateManager.currentState.value)

        AvatarStateManager.onApologizingForMistake()
        assertEquals(AvatarState.APOLOGETIC, AvatarStateManager.currentState.value)

        AvatarStateManager.onTaskCompleted(true)
        assertEquals(AvatarState.TASK_SUCCESS, AvatarStateManager.currentState.value)

        AvatarStateManager.returnToIdle()
        assertEquals(AvatarState.IDLE, AvatarStateManager.currentState.value)
    }

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
        val response = GeminiClient.generateLocalFallbackResponse("Hello Yesmin", NovaPersona.GENERAL.name)
        assertTrue(response.isNotEmpty())
        assertTrue(response.contains("Nova AI") || response.contains("Yesmin"))
    }
}
