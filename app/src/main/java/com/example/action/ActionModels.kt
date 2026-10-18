package com.example.action

enum class TargetApp(val displayName: String, val packageName: String?) {
    WHATSAPP("WhatsApp", "com.whatsapp"),
    SMS("SMS / Messages", null),
    EMAIL("Email", null)
}

enum class ActionIntent {
    SEND_MESSAGE,
    SET_ALARM,
    CREATE_CALENDAR_EVENT,
    SET_REMINDER,
    BLUETOOTH_CONTROL,
    SMART_DEVICE_CONTROL,
    CORRECTION_FEEDBACK,
    UNKNOWN
}

sealed class ParsedAction(val intent: ActionIntent) {
    data class SendMessage(
        val app: TargetApp,
        val recipient: String,
        val exactMessage: String,
        val originalPrompt: String
    ) : ParsedAction(ActionIntent.SEND_MESSAGE)

    data class SetAlarm(
        val hour: Int,
        val minute: Int,
        val label: String
    ) : ParsedAction(ActionIntent.SET_ALARM)

    data class CreateCalendarEvent(
        val title: String,
        val startEpochMillis: Long,
        val endEpochMillis: Long,
        val description: String = ""
    ) : ParsedAction(ActionIntent.CREATE_CALENDAR_EVENT)

    data class SetReminder(
        val title: String,
        val triggerEpochMillis: Long
    ) : ParsedAction(ActionIntent.SET_REMINDER)

    data class BluetoothControl(
        val command: String, // "CONNECT", "STATUS", "DISCONNECT"
        val deviceName: String? = null
    ) : ParsedAction(ActionIntent.BLUETOOTH_CONTROL)

    data class SmartDeviceControl(
        val deviceName: String,
        val command: String // "ON", "OFF", "TOGGLE"
    ) : ParsedAction(ActionIntent.SMART_DEVICE_CONTROL)

    data class CorrectionFeedback(
        val taskType: String,
        val complaint: String,
        val expectedCorrection: String
    ) : ParsedAction(ActionIntent.CORRECTION_FEEDBACK)
}

data class ActionValidationResult(
    val isValid: Boolean,
    val sanitizedAction: ParsedAction,
    val warnings: List<String> = emptyList(),
    val appliedCorrectionRule: String? = null
)
