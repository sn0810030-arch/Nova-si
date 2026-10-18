package com.example.action

import com.example.data.local.entity.CorrectionEntity

object ActionValidator {

    fun validate(
        action: ParsedAction,
        activeCorrections: List<CorrectionEntity>
    ): ActionValidationResult {
        when (action) {
            is ParsedAction.SendMessage -> {
                var message = action.exactMessage.trim()
                val warnings = mutableListOf<String>()
                var appliedRule: String? = null

                // Look for relevant corrections
                val matchingRule = activeCorrections.firstOrNull {
                    it.taskType == "whatsapp_message" || it.taskType == "sms_message"
                }

                if (matchingRule != null) {
                    appliedRule = matchingRule.rule
                    // Enforce rule: Strip any accidental greeting or recipient name prefix
                    if (message.startsWith("Hi ${action.recipient}", ignoreCase = true)) {
                        message = message.substringAfter(action.recipient).trim().removePrefix(",").trim()
                        warnings.add("Enforced correction rule: Removed recipient name from message body.")
                    }
                    if (message.startsWith("Hello ${action.recipient}", ignoreCase = true)) {
                        message = message.substringAfter(action.recipient).trim().removePrefix(",").trim()
                        warnings.add("Enforced correction rule: Removed recipient name from message body.")
                    }
                }

                // Recipient check
                if (action.recipient.isBlank()) {
                    return ActionValidationResult(
                        isValid = false,
                        sanitizedAction = action,
                        warnings = listOf("Recipient name cannot be blank. Please specify who to send to.")
                    )
                }

                if (message.isBlank()) {
                    return ActionValidationResult(
                        isValid = false,
                        sanitizedAction = action,
                        warnings = listOf("Message body cannot be empty.")
                    )
                }

                val sanitized = action.copy(exactMessage = message)
                return ActionValidationResult(
                    isValid = true,
                    sanitizedAction = sanitized,
                    warnings = warnings,
                    appliedCorrectionRule = appliedRule
                )
            }

            is ParsedAction.SetAlarm -> {
                val isValidHour = action.hour in 0..23
                val isValidMinute = action.minute in 0..59
                return ActionValidationResult(
                    isValid = isValidHour && isValidMinute,
                    sanitizedAction = action,
                    warnings = if (!isValidHour || !isValidMinute) listOf("Invalid time for alarm.") else emptyList()
                )
            }

            is ParsedAction.SetReminder -> {
                val isFuture = action.triggerEpochMillis > System.currentTimeMillis() - 60000
                return ActionValidationResult(
                    isValid = isFuture && action.title.isNotBlank(),
                    sanitizedAction = action,
                    warnings = if (!isFuture) listOf("Reminder time is in the past.") else emptyList()
                )
            }

            else -> {
                return ActionValidationResult(
                    isValid = true,
                    sanitizedAction = action
                )
            }
        }
    }
}
