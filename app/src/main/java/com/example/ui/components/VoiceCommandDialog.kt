package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCosmicBg
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanGlow
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceCardBorder
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceCommandDialog(
    isListening: Boolean,
    lastHeardText: String,
    autoSpeakResponse: Boolean,
    onAutoSpeakChange: (Boolean) -> Unit,
    onStartListening: () -> Unit,
    onExecuteCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    val sampleVoiceCommands = listOf(
        "\"New chat\"",
        "\"Switch to code mode\"",
        "\"Switch to writing mode\"",
        "\"Switch to tutor mode\"",
        "\"Explain Kotlin Coroutines\"",
        "\"Read aloud\"",
        "\"Stop speaking\"",
        "\"Clear chat\""
    )

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
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Voice Commands",
                        tint = NovaCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Nova Voice Control",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).testTag("close_voice_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = NovaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Glowing Animated Mic Circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer pulsing glow ring
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color(0x3300E5FF))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (isListening) listOf(NovaCyan, NovaPurple)
                                    else listOf(Color(0xFF1E293B), Color(0xFF334155))
                                )
                            )
                            .border(2.dp, if (isListening) NovaCyanGlow else NovaSurfaceCardBorder, CircleShape)
                            .clickable { onStartListening() }
                            .testTag("voice_dialog_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Tap to speak",
                            tint = if (isListening) NovaCosmicBg else NovaCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = if (isListening) "Listening for commands or questions…" else "Tap microphone to speak",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (isListening) NovaCyan else NovaTextSecondary
                    ),
                    textAlign = TextAlign.Center
                )

                if (lastHeardText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NovaSurfaceDark)
                            .border(1.dp, NovaCyan, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Recognized: \"$lastHeardText\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NovaTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto Read Aloud Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NovaSurfaceDark)
                        .clickable { onAutoSpeakChange(!autoSpeakResponse) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = autoSpeakResponse,
                        onCheckedChange = onAutoSpeakChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = NovaCyan,
                            checkmarkColor = NovaCosmicBg,
                            uncheckedColor = NovaTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = NovaCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Read voice answers aloud (TTS)",
                        style = MaterialTheme.typography.bodySmall.copy(color = NovaTextPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Supported Commands (Tap to test):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = NovaTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleVoiceCommands.forEach { cmd ->
                        val clean = cmd.replace("\"", "")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NovaSurfaceDark)
                                .border(1.dp, NovaSurfaceCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    onExecuteCommand(clean)
                                    onDismiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("sample_voice_cmd_${clean.replace(" ", "_")}")
                        ) {
                            Text(
                                text = cmd,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NovaCyan,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onStartListening,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyan,
                    contentColor = NovaCosmicBg
                ),
                modifier = Modifier.testTag("dialog_listen_now_button")
            ) {
                Text(if (isListening) "Listening…" else "Speak Now")
            }
        }
    )
}
