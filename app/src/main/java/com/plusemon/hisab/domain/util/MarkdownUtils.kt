package com.plusemon.hisab.domain.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

object MarkdownUtils {

    fun parseMarkdown(input: String): AnnotatedString {
        if (input.isBlank()) return AnnotatedString("")

        return buildAnnotatedString {
            val lines = input.lines()
            lines.forEachIndexed { index, line ->
                var trimmedLine = line.trim()

                var isHeader = false
                if (trimmedLine.startsWith("#")) {
                    isHeader = true
                    trimmedLine = trimmedLine.dropWhile { it == '#' }.trim()
                }

                if (trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ")) {
                    trimmedLine = "• " + trimmedLine.substring(2)
                }

                if (isHeader) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        appendFormattedLine(trimmedLine)
                    }
                } else {
                    appendFormattedLine(trimmedLine)
                }

                if (index < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendFormattedLine(line: String) {
        val regex = Regex("\\*\\*(.*?)\\*\\*|__(.*?)__")
        var lastIndex = 0

        for (match in regex.findAll(line)) {
            if (match.range.first > lastIndex) {
                append(line.substring(lastIndex, match.range.first))
            }

            val boldText = match.groups[1]?.value ?: match.groups[2]?.value ?: ""
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(boldText)
            }

            lastIndex = match.range.last + 1
        }

        if (lastIndex < line.length) {
            append(line.substring(lastIndex))
        }
    }

    fun deduplicateReleaseNotes(notes: String): String {
        if (notes.isBlank()) return notes
        val lines = notes.lines()
        val distinctLines = mutableListOf<String>()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                if (distinctLines.isNotEmpty() && distinctLines.last().isNotBlank()) {
                    distinctLines.add("")
                }
            } else if (distinctLines.none { it.trim() == trimmed }) {
                distinctLines.add(line)
            }
        }
        return distinctLines.joinToString("\n").trim()
    }
}
