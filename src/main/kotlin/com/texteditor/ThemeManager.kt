package com.texteditor

/** テーマ管理クラス ダークモードとライトモードの切り替えを管理 */
class ThemeManager {
    private var isDarkMode: Boolean = false

    /** 現在のシステムテーマに基づいて初期テーマを設定 */
    init {
        // デフォルトはライトテーマ（将来的にOS検出に拡張可能）
        this.isDarkMode = false
    }

    /** テーマを切り替え */
    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }

    /** 現在のテーマがダークモードかどうか */
    fun isDarkMode(): Boolean = isDarkMode
}
