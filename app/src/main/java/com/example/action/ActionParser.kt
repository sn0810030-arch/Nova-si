package com.example.action

import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object ActionParser {

    /**
     * Parse raw natural language user prompt into a structured action or null if not an action.
     */
    fun parse(prompt: String): ParsedAction? {
        val trimmed = prompt.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Check for Correction Feedback
        val correctionAction = checkCorrectionFeedback(trimmed, lower)
        if (correctionAction != null) return correctionAction

        // 2. Check for Message Sending (WhatsApp / SMS / Email)
        val messageAction = parseMessageAction(trimmed, lower)
        if (messageAction != null) return messageAction

        // 3. Check for Alarm Setting
        val alarmAction = parseAlarmAction(trimmed, lower)
        if (alarmAction != null) return alarmAction

        // 4. Check for Reminder / Calendar
        val reminderAction = parseReminderAction(trimmed, lower)
        if (reminderAction != null) return reminderAction

        // 5. Check for Bluetooth Control
        val bluetoothAction = parseBluetoothAction(trimmed, lower)
        if (bluetoothAction != null) return bluetoothAction

        // 6. Check for Smart Device Control
        val smartDeviceAction = parseSmartDeviceAction(trimmed, lower)
        if (smartDeviceAction != null) return smartDeviceAction

        return null
    }

    private fun checkCorrectionFeedback(original: String, lower: String): ParsedAction.CorrectionFeedback? {
        val complaintTriggers = listOf(
            "wrong message",
            "incorrect message",
            "didn't ask for that",
            "did not ask for that",
            "only asked for",
            "don't append",
            "do not append",
            "too much text",
            "extra text",
            "mistake",
            "galat message",
            "maine sirf",
            "sirf yeh bhejo",
            "don't add extra",
            "that was wrong"
        )

        val isComplaint = complaintTriggers.any { lower.contains(it) }
        if (!isComplaint) return null

        val taskType = when {
            lower.contains("whatsapp") -> "whatsapp_message"
            lower.contains("sms") -> "sms_message"
            lower.contains("alarm") -> "alarm_set"
            lower.contains("calendar") || lower.contains("meeting") -> "calendar_event"
            else -> "general_response"
        }

        // Extract expected correction if provided
        val quotes = extractQuotedContent(original)
        val expected = quotes.firstOrNull() ?: original.substringAfter("only", "").substringAfter("sirf", "").trim()

        return ParsedAction.CorrectionFeedback(
            taskType = taskType,
            complaint = original,
            expectedCorrection = expected.ifBlank { "Send only the exact message explicitly requested" }
        )
    }

    /**
     * Strict message parser: isolates Recipient and Exact Message Body.
     * Guaranteed never to concatenate recipient into message body or add greetings.
     */
    fun parseMessageAction(original: String, lower: String): ParsedAction.SendMessage? {
        val isWhatsApp = lower.contains("whatsapp")
        val isSms = lower.contains("sms") || lower.contains("text message") || lower.contains("text to")
        val isEmail = lower.contains("email") || lower.contains("mail")

        if (!isWhatsApp && !isSms && !isEmail) {
            // Also check generic "send message to X saying Y"
            if (!lower.startsWith("send ") && !lower.contains("message karo")) {
                return null
            }
        }

        val targetApp = when {
            isWhatsApp -> TargetApp.WHATSAPP
            isEmail -> TargetApp.EMAIL
            else -> TargetApp.SMS
        }

        // Check for explicitly quoted message first: e.g. send Rahul WhatsApp "Hi"
        val quoted = extractQuotedContent(original)
        if (quoted.isNotEmpty()) {
            val recipient = extractRecipient(original, lower, quoted.first())
            if (recipient.isNotBlank()) {
                return ParsedAction.SendMessage(
                    app = targetApp,
                    recipient = recipient,
                    exactMessage = quoted.first(),
                    originalPrompt = original
                )
            }
        }

        // Pattern 1 (Hinglish): "Rahul ko WhatsApp par Hi message karo" or "Rahul ko WhatsApp pe Hi bhejo"
        val hinglishPattern = Pattern.compile(
            "^(?:yesmin[,\\s]*)?(.*?)\\s+ko\\s+(?:whatsapp|sms|email)?(?:\\s+par|\\s+pe)?\\s+(.*?)(?:\\s+message\\s+karo|\\s+bhejo|\\s+send\\s+karo)$",
            Pattern.CASE_INSENSITIVE
        )
        val hinglishMatcher = hinglishPattern.matcher(original.trim())
        if (hinglishMatcher.matches()) {
            val rec = cleanName(hinglishMatcher.group(1) ?: "")
            val msg = cleanMessageBody(hinglishMatcher.group(2) ?: "", rec)
            if (rec.isNotBlank() && msg.isNotBlank()) {
                return ParsedAction.SendMessage(
                    app = targetApp,
                    recipient = rec,
                    exactMessage = msg,
                    originalPrompt = original
                )
            }
        }

        // Pattern 2 (English): "send <Recipient> a <App> message saying <Message>"
        val englishPattern = Pattern.compile(
            "^(?:yesmin[,\\s]*)?(?:please\\s+)?send\\s+(.*?)\\s+(?:a|an)?\\s*(?:whatsapp|sms|text|email)?\\s*(?:message)?\\s*(?:saying|that)\\s+(.*)$",
            Pattern.CASE_INSENSITIVE
        )
        val englishMatcher = englishPattern.matcher(original.trim())
        if (englishMatcher.matches()) {
            val rec = cleanName(englishMatcher.group(1) ?: "")
            val msg = cleanMessageBody(englishMatcher.group(2) ?: "", rec)
            if (rec.isNotBlank() && msg.isNotBlank()) {
                return ParsedAction.SendMessage(
                    app = targetApp,
                    recipient = rec,
                    exactMessage = msg,
                    originalPrompt = original
                )
            }
        }

        // Pattern 3: "WhatsApp <Recipient>: <Message>"
        val colonPattern = Pattern.compile(
            "^(?:yesmin[,\\s]*)?(?:whatsapp|sms|message|text)\\s+(.*?)\\s*:\\s*(.*)$",
            Pattern.CASE_INSENSITIVE
        )
        val colonMatcher = colonPattern.matcher(original.trim())
        if (colonMatcher.matches()) {
            val rec = cleanName(colonMatcher.group(1) ?: "")
            val msg = cleanMessageBody(colonMatcher.group(2) ?: "", rec)
            if (rec.isNotBlank() && msg.isNotBlank()) {
                return ParsedAction.SendMessage(
                    app = targetApp,
                    recipient = rec,
                    exactMessage = msg,
                    originalPrompt = original
                )
            }
        }

        return null
    }

    private fun extractRecipient(original: String, lower: String, quotedText: String): String {
        val withoutQuote = original.replace("\"$quotedText\"", "").replace("'$quotedText'", "").trim()
        val lowerWithoutQuote = withoutQuote.lowercase()

        // "send <Recipient> on whatsapp"
        if (lowerWithoutQuote.contains("ko")) {
            return cleanName(withoutQuote.substringBefore("ko").substringAfter("yesmin,").substringAfter("yesmin").trim())
        }
        if (lowerWithoutQuote.contains("to ")) {
            val candidate = withoutQuote.substringAfter("to ").substringBefore(" on").substringBefore(" via").substringBefore(" a ").trim()
            return cleanName(candidate)
        }
        if (lowerWithoutQuote.startsWith("send ")) {
            val candidate = withoutQuote.substringAfter("send ").substringBefore(" a ").substringBefore(" on ").substringBefore(" via ").substringBefore(" whatsapp").trim()
            return cleanName(candidate)
        }
        return ""
    }

    private fun cleanName(raw: String): String {
        return raw.replace(Regex("^(yesmin|nova|hey|please|can you)\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*(ko|to|on|whatsapp|sms)$", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun cleanMessageBody(raw: String, recipientName: String): String {
        var cleaned = raw.trim()
        // Strip surrounding quotation marks if present
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            if (cleaned.length >= 2) {
                cleaned = cleaned.substring(1, cleaned.length - 1).trim()
            }
        }

        // CRITICAL BUG FIX: Ensure recipient name wasn't accidentally prepended
        if (recipientName.isNotBlank() && cleaned.startsWith(recipientName, ignoreCase = true)) {
            val withoutRec = cleaned.substring(recipientName.length).trim()
            if (withoutRec.startsWith(",") || withoutRec.startsWith(":")) {
                cleaned = withoutRec.substring(1).trim()
            }
        }

        // Clean out dangling "message karo" or "bhejo"
        cleaned = cleaned.replace(Regex("\\s*(message karo|bhejo|send karo)$", RegexOption.IGNORE_CASE), "").trim()
        return cleaned
    }

    private fun extractQuotedContent(text: String): List<String> {
        val list = mutableListOf<String>()
        val p = Pattern.compile("[\"']([^\"']+)[\"']")
        val m = p.matcher(text)
        while (m.find()) {
            m.group(1)?.let { list.add(it.trim()) }
        }
        return list
    }

    private fun parseAlarmAction(original: String, lower: String): ParsedAction.SetAlarm? {
        if (!lower.contains("alarm")) return null

        val timePattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE)
        val m = timePattern.matcher(lower)
        if (m.find()) {
            var hour = m.group(1)?.toIntOrNull() ?: return null
            val minute = m.group(2)?.toIntOrNull() ?: 0
            val amPm = m.group(3)?.lowercase()

            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0

            return ParsedAction.SetAlarm(
                hour = hour,
                minute = minute,
                label = "Nova Alarm"
            )
        }
        return null
    }

    private fun parseReminderAction(original: String, lower: String): ParsedAction.SetReminder? {
        val isReminder = lower.contains("remind") || lower.contains("schedule")
        if (!isReminder) return null

        val cal = Calendar.getInstance()
        if (lower.contains("tomorrow") || lower.contains("kal")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val timePattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)", Pattern.CASE_INSENSITIVE)
        val m = timePattern.matcher(lower)
        if (m.find()) {
            var hour = m.group(1)?.toIntOrNull() ?: 9
            val minute = m.group(2)?.toIntOrNull() ?: 0
            val amPm = m.group(3)?.lowercase()
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
        } else {
            cal.add(Calendar.HOUR_OF_DAY, 1) // default 1 hr later
        }

        // Title of task
        val title = when {
            lower.contains("to ") -> original.substringAfter("to ").trim()
            lower.contains("ki ") -> original.substringAfter("ki ").trim()
            else -> "Reminder"
        }

        return ParsedAction.SetReminder(
            title = title.ifBlank { "Reminder" },
            triggerEpochMillis = cal.timeInMillis
        )
    }

    private fun parseBluetoothAction(original: String, lower: String): ParsedAction.BluetoothControl? {
        if (!lower.contains("bluetooth") && !lower.contains("headphones") && !lower.contains("speaker")) return null

        val command = when {
            lower.contains("connect") -> "CONNECT"
            lower.contains("disconnect") -> "DISCONNECT"
            lower.contains("show") || lower.contains("list") || lower.contains("status") -> "STATUS"
            else -> "STATUS"
        }

        val deviceName = if (lower.contains("headphones")) "Headphones" else if (lower.contains("speaker")) "Speaker" else null
        return ParsedAction.BluetoothControl(command = command, deviceName = deviceName)
    }

    private fun parseSmartDeviceAction(original: String, lower: String): ParsedAction.SmartDeviceControl? {
        if (!lower.contains("light") && !lower.contains("plug") && !lower.contains("fan") && !lower.contains("smart")) return null

        val command = when {
            lower.contains("turn on") || lower.contains("switch on") || lower.contains("on") -> "ON"
            lower.contains("turn off") || lower.contains("switch off") || lower.contains("off") -> "OFF"
            else -> "TOGGLE"
        }

        val deviceName = when {
            lower.contains("living room") -> "Living Room Light"
            lower.contains("bedroom") -> "Bedroom Light"
            lower.contains("fan") -> "Smart Fan"
            else -> "Smart Light"
        }

        return ParsedAction.SmartDeviceControl(deviceName = deviceName, command = command)
    }
}
