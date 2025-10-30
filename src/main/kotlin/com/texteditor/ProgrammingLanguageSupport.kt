package com.texteditor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/** プログラミング言語サポートクラス Go, C/C++, Haskell, Javaのシンタックスハイライトを提供 */
enum class Language {
    JAVA,
    GO,
    CPP,
    HASKELL,
    MARKDOWN,
    PLAIN_TEXT
}

class ProgrammingLanguageSupport {

    // Java構文パターン
    private val javaPatterns =
        listOf(
            "(?<JAVAKEYWORD>\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while)\\b)",
            "(?<JAVASTRING>\"([^\"\\\\]|\\\\.)*\")",
            "(?<JAVACOMMENT>//[^\\r\\n]*|/\\*[\\s\\S]*?\\*/)",
            "(?<JAVANUMBER>\\b\\d+(\\.\\d+)?[fFdDlL]?\\b)"
        )

    // Go構文パターン
    private val goPatterns =
        listOf(
            "(?<GOKEYWORD>\\b(break|case|chan|const|continue|default|defer|else|fallthrough|for|func|go|goto|if|import|interface|map|package|range|return|select|struct|switch|type|var)\\b)",
            "(?<GOSTRING>\"([^\"\\\\]|\\\\.)*\"|`[^`]*`)",
            "(?<GOCOMMENT>//[^\\r\\n]*|/\\*[\\s\\S]*?\\*/)",
            "(?<GONUMBER>\\b\\d+(\\.\\d+)?\\b)"
        )

    // C/C++構文パターン
    private val cppPatterns =
        listOf(
            "(?<CPPKEYWORD>\\b(auto|break|case|char|const|continue|default|do|double|else|enum|extern|float|for|goto|if|inline|int|long|register|return|short|signed|sizeof|static|struct|switch|typedef|union|unsigned|void|volatile|while|class|namespace|template|typename|virtual|public|private|protected|using|try|catch|throw|new|delete|this|nullptr)\\b)",
            "(?<CPPSTRING>\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*')",
            "(?<CPPCOMMENT>//[^\\r\\n]*|/\\*[\\s\\S]*?\\*/)",
            "(?<CPPNUMBER>\\b\\d+(\\.\\d+)?[fFlLuU]*\\b)",
            "(?<CPPPREPROCESSOR>#[^\\r\\n]*)"
        )

    // Haskell構文パターン
    private val haskellPatterns =
        listOf(
            "(?<HASKELLKEYWORD>\\b(case|class|data|default|deriving|do|else|foreign|if|import|in|infix|infixl|infixr|instance|let|module|newtype|of|then|type|where|as|qualified|hiding)\\b)",
            "(?<HASKELLSTRING>\"([^\"\\\\]|\\\\.)*\")",
            "(?<HASKELLCOMMENT>--[^\\r\\n]*|\\{-[\\s\\S]*?-\\})",
            "(?<HASKELLNUMBER>\\b\\d+(\\.\\d+)?\\b)",
            "(?<HASKELLOPERATOR>[=><+\\-*/&|!@#$%^&*()\\[\\]{}.,;:])"
        )

    private var currentLanguage: Language = Language.MARKDOWN

    /** 現在の言語を設定 */
    fun setLanguage(language: Language) {
        this.currentLanguage = language
    }

    /** 現在の言語を取得 */
    fun getCurrentLanguage(): Language = currentLanguage

    /** テキストのシンタックスハイライトを計算 */
    fun computeHighlighting(text: String, isDarkMode: Boolean = false): AnnotatedString {
        return when (currentLanguage) {
            Language.JAVA -> computeJavaHighlighting(text, isDarkMode)
            Language.GO -> computeGoHighlighting(text, isDarkMode)
            Language.CPP -> computeCppHighlighting(text, isDarkMode)
            Language.HASKELL -> computeHaskellHighlighting(text, isDarkMode)
            Language.MARKDOWN -> MarkdownHighlighter().computeHighlighting(text, isDarkMode)
            Language.PLAIN_TEXT -> computePlainTextHighlighting(text, isDarkMode)
        }
    }

    private fun computeJavaHighlighting(text: String, isDarkMode: Boolean): AnnotatedString {
        return computeHighlightingForPatterns(text, javaPatterns, "JAVA", isDarkMode)
    }

    private fun computeGoHighlighting(text: String, isDarkMode: Boolean): AnnotatedString {
        return computeHighlightingForPatterns(text, goPatterns, "GO", isDarkMode)
    }

    private fun computeCppHighlighting(text: String, isDarkMode: Boolean): AnnotatedString {
        return computeHighlightingForPatterns(text, cppPatterns, "CPP", isDarkMode)
    }

    private fun computeHaskellHighlighting(text: String, isDarkMode: Boolean): AnnotatedString {
        return computeHighlightingForPatterns(text, haskellPatterns, "HASKELL", isDarkMode)
    }

    private fun computePlainTextHighlighting(text: String, isDarkMode: Boolean): AnnotatedString {
        val colors = if (isDarkMode) DarkThemeColors else LightThemeColors
        return buildAnnotatedString { withStyle(SpanStyle(color = colors.text)) { append(text) } }
    }

    private fun computeHighlightingForPatterns(
        text: String,
        patterns: List<String>,
        prefix: String,
        isDarkMode: Boolean
    ): AnnotatedString {
        val pattern = Regex(patterns.joinToString("|"))
        val matches = pattern.findAll(text).toList()
        val spans = mutableListOf<Pair<IntRange, SpanStyle>>()

        var lastEnd = 0
        for (match in matches) {
            if (match.range.first > lastEnd) {
                spans.add(lastEnd until match.range.first to getDefaultStyle(isDarkMode))
            }

            val style = getStyleClassForMatch(match, prefix, isDarkMode)
            spans.add(match.range to style)
            lastEnd = match.range.last + 1
        }

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

    private fun getStyleClassForMatch(
        match: MatchResult,
        prefix: String,
        isDarkMode: Boolean
    ): SpanStyle {
        val colors = if (isDarkMode) DarkThemeColors else LightThemeColors

        return when {
            match.groups["${prefix}KEYWORD"] != null ->
                SpanStyle(color = colors.keyword, fontWeight = FontWeight.Bold)
            match.groups["${prefix}STRING"] != null -> SpanStyle(color = colors.string)
            match.groups["${prefix}COMMENT"] != null ->
                SpanStyle(color = colors.comment, fontStyle = FontStyle.Italic)
            match.groups["${prefix}NUMBER"] != null -> SpanStyle(color = colors.number)
            prefix == "CPP" && match.groups["CPPPREPROCESSOR"] != null ->
                SpanStyle(color = colors.preprocessor)
            prefix == "HASKELL" && match.groups["HASKELLOPERATOR"] != null ->
                SpanStyle(color = colors.operator)
            else -> getDefaultStyle(isDarkMode)
        }
    }

    private fun getDefaultStyle(isDarkMode: Boolean): SpanStyle {
        val colors = if (isDarkMode) DarkThemeColors else LightThemeColors
        return SpanStyle(color = colors.text)
    }
}

// プログラミング言語用のカラー定義
