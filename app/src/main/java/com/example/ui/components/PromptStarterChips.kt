package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NovaPersona
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceCardBorder
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

fun getPersonaIcon(iconName: String): ImageVector {
    return when (iconName) {
        "Code" -> Icons.Default.Code
        "Edit" -> Icons.Default.Edit
        "School" -> Icons.Default.School
        "Palette" -> Icons.Default.Palette
        else -> Icons.Default.AutoAwesome
    }
}

@Composable
fun PromptStarterChips(
    persona: NovaPersona,
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Suggested Prompts",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = NovaTextSecondary,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        persona.suggestedPrompts.forEachIndexed { index, prompt ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NovaSurfaceCard)
                    .border(1.dp, NovaSurfaceCardBorder, RoundedCornerShape(14.dp))
                    .clickable { onPromptSelected(prompt) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("prompt_starter_chip_$index")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = getPersonaIcon(persona.iconName),
                        contentDescription = null,
                        tint = NovaCyan,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Normal
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Send prompt",
                        tint = NovaTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
