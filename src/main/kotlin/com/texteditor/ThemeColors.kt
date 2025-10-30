package com.texteditor

import androidx.compose.ui.graphics.Color

// テーマカラー定義インターフェース
interface ThemeColors {
    val heading: Color
    val text: Color
    val codeInline: Color
    val codeBlock: Color
    val codeBackground: Color
    val quote: Color
    val link: Color
    val image: Color
    val strikethrough: Color
    val horizontal: Color
    val keyword: Color
    val string: Color
    val comment: Color
    val number: Color
    val preprocessor: Color
    val operator: Color
    val background: Color
    val surface: Color
}

object LightThemeColors : ThemeColors {
    override val heading = Color(0xFF2E74B5)
    override val text = Color(0xFF323130)
    override val codeInline = Color(0xFFE36209)
    override val codeBlock = Color(0xFF24292E)
    override val codeBackground = Color(0xFFF6F8FA)
    override val quote = Color(0xFF6A737D)
    override val link = Color(0xFF0366D6)
    override val image = Color(0xFF28A745)
    override val strikethrough = Color(0xFF6A737D)
    override val horizontal = Color(0xFFEAECEF)
    override val keyword = Color(0xFF0000FF)
    override val string = Color(0xFF008000)
    override val comment = Color(0xFF808080)
    override val number = Color(0xFFFF8000)
    override val preprocessor = Color(0xFF800080)
    override val operator = Color(0xFFFF0000)
    override val background = Color(0xFFFFFFFF)
    override val surface = Color(0xFFF8F8F8)
}

object DarkThemeColors : ThemeColors {
    override val heading = Color(0xFF4FC3F7)
    override val text = Color(0xFFF0F0F0)
    override val codeInline = Color(0xFFF97583)
    override val codeBlock = Color(0xFFF0F0F0)
    override val codeBackground = Color(0xFF3A3A3A)
    override val quote = Color(0xFF8B949E)
    override val link = Color(0xFF58A6FF)
    override val image = Color(0xFF28A745)
    override val strikethrough = Color(0xFF8B949E)
    override val horizontal = Color(0xFFEAECEF)
    override val keyword = Color(0xFF569CD6)
    override val string = Color(0xFFCE9178)
    override val comment = Color(0xFF6A9955)
    override val number = Color(0xFFB5CEA8)
    override val preprocessor = Color(0xFFC586C0)
    override val operator = Color(0xFFDCDCAA)
    override val background = Color(0xFF1E1E1E)
    override val surface = Color(0xFF2B2B2B)
}
