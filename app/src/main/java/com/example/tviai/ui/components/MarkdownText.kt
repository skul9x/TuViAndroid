package com.example.tviai.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Simple Markdown Text renderer for Gemini AI output.
 * Supports: **bold**, *italic*, # headings, ## subheadings, ### h3
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = remember(text) {
        buildAnnotatedString {
            val lines = text.lines()
            
            for ((index, line) in lines.withIndex()) {
                when {
                    // H1 - Main heading
                    line.trimStart().startsWith("# ") && !line.trimStart().startsWith("##") -> {
                        withStyle(SpanStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB8860B)
                        )) {
                            append(line.trimStart().removePrefix("# ").trim())
                        }
                        append("\n")
                    }
                    // H2 - Subheading
                    line.trimStart().startsWith("## ") && !line.trimStart().startsWith("###") -> {
                        withStyle(SpanStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B4513)
                        )) {
                            append(line.trimStart().removePrefix("## ").trim())
                        }
                        append("\n")
                    }
                    // H3
                    line.trimStart().startsWith("### ") -> {
                        withStyle(SpanStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )) {
                            append(line.trimStart().removePrefix("### ").trim())
                        }
                        append("\n")
                    }
                    // Regular line with inline formatting
                    else -> {
                        parseInlineFormatting(this, line)
                        if (index < lines.size - 1) append("\n")
                    }
                }
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 26.sp,
        fontFamily = FontFamily.Serif
    )
}

/**
 * Parse inline markdown: **bold**, *italic*, ***bold italic***
 */
private fun parseInlineFormatting(builder: androidx.compose.ui.text.AnnotatedString.Builder, text: String) {
    var i = 0
    val len = text.length

    while (i < len) {
        when {
            // Bold italic: ***text***
            i + 2 < len && text.substring(i, i + 3) == "***" -> {
                val endIdx = text.indexOf("***", i + 3)
                if (endIdx != -1) {
                    builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 3, endIdx))
                    }
                    i = endIdx + 3
                } else {
                    builder.append(text[i])
                    i++
                }
            }
            // Bold: **text**
            i + 1 < len && text.substring(i, i + 2) == "**" -> {
                val endIdx = text.indexOf("**", i + 2)
                if (endIdx != -1) {
                    builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(i + 2, endIdx))
                    }
                    i = endIdx + 2
                } else {
                    builder.append(text[i])
                    i++
                }
            }
            // Italic: *text*
            text[i] == '*' && i + 1 < len && text[i + 1] != '*' -> {
                val endIdx = text.indexOf("*", i + 1)
                if (endIdx != -1 && endIdx > i + 1) {
                    builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, endIdx))
                    }
                    i = endIdx + 1
                } else {
                    builder.append(text[i])
                    i++
                }
            }
            // Regular character
            else -> {
                builder.append(text[i])
                i++
            }
        }
    }
}
