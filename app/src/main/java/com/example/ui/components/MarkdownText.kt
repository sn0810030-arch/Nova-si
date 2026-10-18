package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaPurpleLight
import com.example.ui.theme.NovaTextPrimary

private sealed class MarkdownSegment {
    data class Code(val code: String, val language: String) : MarkdownSegment()
    data class Text(val lines: List<String>) : MarkdownSegment()
}

@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = NovaTextPrimary
) {
    val segments = parseMarkdownSegments(content)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (segment in segments) {
            when (segment) {
                is MarkdownSegment.Code -> {
                    CodeBlockView(
                        code = segment.code,
                        language = segment.language
                    )
                }
                is MarkdownSegment.Text -> {
                    RenderTextLines(lines = segment.lines, textColor = textColor)
                }
            }
        }
    }
}

@Composable
private fun RenderTextLines(lines: List<String>, textColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("### ") -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trimmed.removePrefix("### "),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NovaCyan,
                            fontSize = 16.sp
                        )
                    )
                }
                trimmed.startsWith("## ") -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = trimmed.removePrefix("## "),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NovaCyan,
                            fontSize = 18.sp
                        )
                    )
                }
                trimmed.startsWith("# ") -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = trimmed.removePrefix("# "),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = NovaPurpleLight,
                            fontSize = 20.sp
                        )
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NovaCyan)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formatInlineMarkdown(trimmed.substring(2)),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
                trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val dotIndex = trimmed.indexOf('.')
                    val number = trimmed.substring(0, dotIndex + 1)
                    val rest = trimmed.substring(dotIndex + 1).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = number,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NovaCyan,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatInlineMarkdown(rest),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textColor,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
                trimmed.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = formatInlineMarkdown(line),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }
    }
}

private fun formatInlineMarkdown(raw: String) = buildAnnotatedString {
    var cursor = 0
    val length = raw.length

    while (cursor < length) {
        val nextBold = raw.indexOf("**", cursor)
        val nextCode = raw.indexOf("`", cursor)

        val nextToken = when {
            nextBold != -1 && nextCode != -1 -> minOf(nextBold, nextCode)
            nextBold != -1 -> nextBold
            nextCode != -1 -> nextCode
            else -> -1
        }

        if (nextToken == -1) {
            append(raw.substring(cursor))
            break
        }

        if (nextToken > cursor) {
            append(raw.substring(cursor, nextToken))
        }

        if (nextToken == nextBold) {
            val endBold = raw.indexOf("**", nextBold + 2)
            if (endBold != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = NovaCyan)) {
                    append(raw.substring(nextBold + 2, endBold))
                }
                cursor = endBold + 2
            } else {
                append("**")
                cursor = nextBold + 2
            }
        } else {
            val endCode = raw.indexOf("`", nextCode + 1)
            if (endCode != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFF1E293B),
                        color = NovaCyan,
                        fontSize = 13.sp
                    )
                ) {
                    append(" " + raw.substring(nextCode + 1, endCode) + " ")
                }
                cursor = endCode + 1
            } else {
                append("`")
                cursor = nextCode + 1
            }
        }
    }
}

private fun parseMarkdownSegments(text: String): List<MarkdownSegment> {
    val segments = mutableListOf<MarkdownSegment>()
    val lines = text.lines()
    var inCodeBlock = false
    var currentLanguage = ""
    val currentCodeLines = mutableListOf<String>()
    val currentTextLines = mutableListOf<String>()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("```")) {
            if (!inCodeBlock) {
                if (currentTextLines.isNotEmpty()) {
                    segments.add(MarkdownSegment.Text(currentTextLines.toList()))
                    currentTextLines.clear()
                }
                inCodeBlock = true
                currentLanguage = trimmed.removePrefix("```").trim()
            } else {
                segments.add(MarkdownSegment.Code(currentCodeLines.joinToString("\n"), currentLanguage))
                currentCodeLines.clear()
                currentLanguage = ""
                inCodeBlock = false
            }
        } else {
            if (inCodeBlock) {
                currentCodeLines.add(line)
            } else {
                currentTextLines.add(line)
            }
        }
    }

    if (inCodeBlock && currentCodeLines.isNotEmpty()) {
        segments.add(MarkdownSegment.Code(currentCodeLines.joinToString("\n"), currentLanguage))
    } else if (currentTextLines.isNotEmpty()) {
        segments.add(MarkdownSegment.Text(currentTextLines.toList()))
    }

    return segments
}
