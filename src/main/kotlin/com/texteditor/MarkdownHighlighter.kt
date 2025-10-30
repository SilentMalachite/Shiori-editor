package com.texteditor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/** Markdown構文ハイライト処理クラス */
class MarkdownHighlighter {

    // Markdown構文の正規表現パターン
    private val markdownPatterns =
        listOf(
            "(?<HEADING1>^#{1}\\s+.*)$", // # 見出し1
            "(?<HEADING2>^#{2}\\s+.*)$", // ## 見出し2
            "(?<HEADING3>^#{3}\\s+.*)$", // ### 見出し3
            "(?<HEADING4>^#{4,6}\\s+.*)$", // #### 見出し4-6
            "(?<BOLD>\\*\\*[^*]+\\*\\*)", // **太字**
            "(?<ITALIC>\\*[^*]+\\*)", // *斜体*
            "(?<CODE>`[^`]+`)", // `コード`
            "(?<CODEBLOCK>```[\\s\\S]*?```)", // ```コードブロック```
            "(?<QUOTE>^>.*$)", // > 引用
            "(?<LINK>\\[[^\\]]*\\]\\([^\\)]*\\))", // [リンク](URL)
            "(?<IMAGE>!\\[[^\\]]*\\]\\([^\\)]*\\))", // ![画像](URL)
            "(?<LIST>^[\\s]*[\\*\\-\\+]\\s+.*$)", // - リスト
            "(?<NUMLIST>^[\\s]*\\d+\\.\\s+.*$)", // 1. 番号付きリスト
            "(?<STRIKETHROUGH>~~[^~]+~~)", // ~~取り消し線~~
            "(?<HORIZONTAL>^[-*_]{3,}$)" // --- 水平線
        )

    private val markdownPattern = Regex(markdownPatterns.joinToString("|"), RegexOption.MULTILINE)

    /** テキストのハイライト情報を計算してAnnotatedStringを返す */
    fun computeHighlighting(text: String, isDarkMode: Boolean = false): AnnotatedString {
        val matches = markdownPattern.findAll(text).toList()
        val spans = mutableListOf<Pair<IntRange, SpanStyle>>()

        var lastEnd = 0
        for (match in matches) {
            // マッチしない部分は通常テキスト
            if (match.range.first > lastEnd) {
                spans.add(lastEnd until match.range.first to getDefaultStyle(isDarkMode))
            }

            // マッチした部分にスタイルを適用
            val style = getStyleForMatch(match, isDarkMode)
            spans.add(match.range to style)
            lastEnd = match.range.last + 1
        }

        // 残りの部分を通常テキストとして追加
        if (lastEnd < text.length) {
            spans.add(lastEnd until text.length to getDefaultStyle(isDarkMode))
        }

        return buildAnnotatedString {
            var currentPos = 0
            for ((range, style) in spans.sortedBy { it.first.first }) {
                if (range.first > currentPos) {
                    append(text.substring(currentPos, range.first))
                }
                if (range.first < text.length && range.last < text.length) {
                    withStyle(style) { append(text.substring(range.first, range.last + 1)) }
                }
                currentPos = range.last + 1
            }
            if (currentPos < text.length) {
                append(text.substring(currentPos))
            }
        }
    }

    /** マッチしたパターンに基づいてスタイルを決定 */
    private fun getStyleForMatch(match: MatchResult, isDarkMode: Boolean): SpanStyle {
        val darkColors = DarkThemeColors
        val lightColors = LightThemeColors
        val colors = if (isDarkMode) darkColors else lightColors

        return when {
            match.groups["HEADING1"] != null ->
                SpanStyle(color = colors.heading, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            match.groups["HEADING2"] != null ->
                SpanStyle(color = colors.heading, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            match.groups["HEADING3"] != null ->
                SpanStyle(color = colors.heading, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            match.groups["HEADING4"] != null ->
                SpanStyle(color = colors.heading, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            match.groups["BOLD"] != null ->
                SpanStyle(color = colors.text, fontWeight = FontWeight.Bold)
            match.groups["ITALIC"] != null ->
                SpanStyle(color = colors.text, fontStyle = FontStyle.Italic)
            match.groups["CODE"] != null ->
                SpanStyle(color = colors.codeInline, background = colors.codeBackground)
            match.groups["CODEBLOCK"] != null ->
                SpanStyle(color = colors.codeBlock, background = colors.codeBackground)
            match.groups["QUOTE"] != null ->
                SpanStyle(color = colors.quote, fontStyle = FontStyle.Italic)
            match.groups["LINK"] != null -> SpanStyle(color = colors.link)
            match.groups["IMAGE"] != null -> SpanStyle(color = colors.image)
            match.groups["LIST"] != null || match.groups["NUMLIST"] != null ->
                SpanStyle(color = colors.text)
            match.groups["STRIKETHROUGH"] != null -> SpanStyle(color = colors.strikethrough)
            match.groups["HORIZONTAL"] != null ->
                SpanStyle(color = colors.horizontal, fontWeight = FontWeight.Bold)
            else -> getDefaultStyle(isDarkMode)
        }
    }

    private fun getDefaultStyle(isDarkMode: Boolean): SpanStyle {
        val colors = if (isDarkMode) DarkThemeColors else LightThemeColors
        return SpanStyle(color = colors.text)
    }
}
