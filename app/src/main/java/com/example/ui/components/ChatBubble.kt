package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.theme.NovaAiBubble
import com.example.ui.theme.NovaAiBubbleBorder
import com.example.ui.theme.NovaCosmicBg
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaUserBubble
import com.example.ui.theme.NovaUserBubbleBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(
    message: ChatMessageEntity,
    onSpeak: (String) -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    if (message.isFromUser) {
        // User Message (Right Aligned)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                        .background(NovaUserBubble)
                        .border(1.dp, NovaUserBubbleBorder, RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("user_message_bubble")
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NovaTextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    )
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NovaTextSecondary,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp, end = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    } else {
        // Nova AI Message (Left Aligned)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(NovaCyan, NovaPurple)))
                    .border(1.dp, Color(0x6600E5FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Nova AI",
                    tint = NovaCosmicBg,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = 520.dp)
            ) {
                // Header (Nova tag + Model)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                ) {
                    Text(
                        text = "Nova",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NovaCyan
                        )
                    )
                    if (message.modelUsed != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${message.modelUsed}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NovaTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $formattedTime",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NovaTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                // Message Body
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                        .background(NovaAiBubble)
                        .border(
                            1.dp,
                            if (message.isError) NovaError else NovaAiBubbleBorder,
                            RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("nova_message_bubble")
                ) {
                    if (message.isError) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = NovaError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = NovaError
                                )
                            )
                        }
                    } else {
                        MarkdownText(content = message.text)
                    }
                }

                // Message Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 2.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Nova response", message.text))
                            Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("copy_response_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = NovaTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSpeak(message.text) },
                        modifier = Modifier.size(32.dp).testTag("speak_response_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = NovaTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, message.text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Nova response"))
                        },
                        modifier = Modifier.size(32.dp).testTag("share_response_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share message",
                            tint = NovaTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (message.isError && onRetry != null) {
                        IconButton(
                            onClick = onRetry,
                            modifier = Modifier.size(32.dp).testTag("retry_response_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                tint = NovaCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingBubble(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(NovaCyan, NovaPurple))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Nova Thinking",
                tint = NovaCosmicBg,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(NovaAiBubble)
                .border(1.dp, NovaAiBubbleBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dot1Scale)
                        .clip(CircleShape)
                        .background(NovaCyan)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dot2Scale)
                        .clip(CircleShape)
                        .background(NovaPurple)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dot3Scale)
                        .clip(CircleShape)
                        .background(NovaCyan)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Nova is thinking…",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NovaTextSecondary,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}
