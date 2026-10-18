package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.action.ActionValidationResult
import com.example.action.ParsedAction
import com.example.ui.theme.NovaCosmicBg
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceCardBorder
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaWarning

@Composable
fun ActionConfirmationDialog(
    validationResult: ActionValidationResult,
    onConfirm: (ParsedAction) -> Unit,
    onDismiss: () -> Unit
) {
    val action = validationResult.sanitizedAction

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NovaSurfaceCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (action) {
                        is ParsedAction.SendMessage -> Icons.AutoMirrored.Filled.Send
                        is ParsedAction.SetAlarm -> Icons.Default.Alarm
                        is ParsedAction.SetReminder -> Icons.Default.NotificationsActive
                        is ParsedAction.CreateCalendarEvent -> Icons.Default.CalendarMonth
                        else -> Icons.Default.CheckCircle
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = NovaCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Action Confirmation",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp).testTag("close_action_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = NovaTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (action) {
                    is ParsedAction.SendMessage -> {
                        Text(
                            text = "Please verify the recipient and exact message content before sending:",
                            style = MaterialTheme.typography.bodySmall.copy(color = NovaTextSecondary)
                        )

                        // Target App & Recipient Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NovaSurfaceDark)
                                .border(1.dp, NovaSurfaceCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Application:", color = NovaTextSecondary, fontSize = 12.sp)
                                    Text(action.app.displayName, color = NovaCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Recipient:", color = NovaTextSecondary, fontSize = 12.sp)
                                    Text(action.recipient, color = NovaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        // Exact Message Card
                        Text(
                            text = "Exact Message (No Extra Text):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NovaCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = action.exactMessage,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Default
                                )
                            )
                        }

                        // Applied Correction Rule banner
                        if (validationResult.appliedCorrectionRule != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x2210B981))
                                    .border(1.dp, NovaSuccess, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Correction Applied: ${validationResult.appliedCorrectionRule}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = NovaSuccess)
                                )
                            }
                        }
                    }

                    is ParsedAction.SetAlarm -> {
                        val amPm = if (action.hour >= 12) "PM" else "AM"
                        val displayHour = if (action.hour % 12 == 0) 12 else action.hour % 12
                        val displayMin = String.format("%02d", action.minute)
                        Text(
                            text = "Set an alarm for $displayHour:$displayMin $amPm?",
                            style = MaterialTheme.typography.bodyMedium.copy(color = NovaTextPrimary)
                        )
                    }

                    is ParsedAction.SetReminder -> {
                        Text(
                            text = "Schedule reminder \"${action.title}\"?",
                            style = MaterialTheme.typography.bodyMedium.copy(color = NovaTextPrimary)
                        )
                    }

                    else -> {
                        Text(
                            text = "Execute this action?",
                            style = MaterialTheme.typography.bodyMedium.copy(color = NovaTextPrimary)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(action)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyan,
                    contentColor = NovaCosmicBg
                ),
                modifier = Modifier.testTag("confirm_action_button")
            ) {
                Text("Confirm & Execute")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_action_button")
            ) {
                Text("Cancel", color = NovaTextSecondary)
            }
        }
    )
}
