package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.NovaPersona
import com.example.ui.theme.NovaCosmicBg
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaCyanGlow
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceCardBorder
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun NovaHeroCard(
    currentPersona: NovaPersona,
    modelName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NovaSurfaceCard)
            .border(1.dp, NovaSurfaceCardBorder, RoundedCornerShape(20.dp))
    ) {
        // Hero Image Background with subtle dark overlay
        Image(
            painter = painterResource(id = R.drawable.nova_hero_banner_1792332458890),
            contentDescription = "Nova AI Hero Banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay for smooth readability
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x800B0F19),
                            Color(0xDF0B0F19),
                            NovaCosmicBg
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NovaCyan, NovaPurple))
                        )
                        .border(1.5.dp, NovaCyanGlow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Nova Core",
                        tint = NovaCosmicBg,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NOVA AI",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                color = NovaCyan
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x3300E5FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "GEMINI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = NovaCyanGlow
                                )
                            )
                        }
                    }

                    Text(
                        text = currentPersona.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NovaTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "What would you like to explore?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextPrimary
                )
            )

            Text(
                text = "Ask questions, analyze concepts, write code, or draft content with ${currentPersona.title}.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = NovaTextSecondary,
                    lineHeight = 18.sp
                )
            )
        }
    }
}
