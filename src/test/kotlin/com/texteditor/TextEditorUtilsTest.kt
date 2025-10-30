package com.texteditor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.*

/** TextEditor ユーティリティ関数のテスト */
class TextEditorUtilsTest {

    @Test
    fun `applyInlineWrap should wrap selected text`() {
        val value = TextFieldValue("Hello World", selection = TextRange(0, 5))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("**Hello** World", result.text)
        assertEquals(2, result.selection.start)
        assertEquals(7, result.selection.end)
    }

    @Test
    fun `applyInlineWrap should insert markers at cursor when no selection`() {
        val value = TextFieldValue("Hello World", selection = TextRange(5))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("Hello**** World", result.text)
        assertEquals(7, result.selection.start)
    }

    @Test
    fun `applyInlineWrap should handle empty text`() {
        val value = TextFieldValue("", selection = TextRange(0))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("****", result.text)
        assertEquals(2, result.selection.start)
    }

    @Test
    fun `applyInlineWrap should work with single character markers`() {
        val value = TextFieldValue("italic", selection = TextRange(0, 6))

        val result = applyInlineWrap("*", "*", value)

        assertEquals("*italic*", result.text)
        assertEquals(1, result.selection.start)
        assertEquals(7, result.selection.end)
    }

    @Test
    fun `applyInlineWrap should work with backticks for code`() {
        val value = TextFieldValue("code", selection = TextRange(0, 4))

        val result = applyInlineWrap("`", "`", value)

        assertEquals("`code`", result.text)
        assertEquals(1, result.selection.start)
        assertEquals(5, result.selection.end)
    }

    @Test
    fun `applyInlineWrap should handle selection at end of text`() {
        val value = TextFieldValue("Hello World", selection = TextRange(6, 11))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("Hello **World**", result.text)
        assertEquals(8, result.selection.start)
        assertEquals(13, result.selection.end)
    }

    @Test
    fun `applyInlineWrap should handle reversed selection`() {
        val value = TextFieldValue("Hello World", selection = TextRange(5, 0))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("**Hello** World", result.text)
        assertEquals(2, result.selection.start)
        assertEquals(7, result.selection.end)
    }

    @Test
    fun `applyInlineWrap should handle selection in middle of text`() {
        val value = TextFieldValue("Hello World Test", selection = TextRange(6, 11))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("Hello **World** Test", result.text)
        assertEquals(8, result.selection.start)
        assertEquals(13, result.selection.end)
    }

    @Test
    fun `getDefaultInitialText should return non-empty text`() {
        val text = getDefaultInitialText()

        assertTrue(text.isNotEmpty())
        assertTrue(text.contains("Markdown対応日本語テキストエディタ"))
    }

    @Test
    fun `getDefaultInitialText should contain basic features`() {
        val text = getDefaultInitialText()

        assertTrue(text.contains("基本機能"))
        assertTrue(text.contains("日本語完全サポート"))
        assertTrue(text.contains("プログラミング言語サポート"))
    }

    @Test
    fun `getDefaultInitialText should contain markdown examples`() {
        val text = getDefaultInitialText()

        assertTrue(text.contains("**"))
        assertTrue(text.contains("*"))
        assertTrue(text.contains("`"))
        assertTrue(text.contains("#"))
    }

    @Test
    fun `confirmSaveIfModified should return true when not modified`() {
        var onSaveCalled = false
        val result = confirmSaveIfModified(false) { onSaveCalled = true }

        assertTrue(result)
        assertFalse(onSaveCalled)
    }

    @Test
    fun `confirmSaveIfModified should return true when modified`() {
        val result = confirmSaveIfModified(true) {}

        assertTrue(result)
        // Note: 現在の実装では簡易版なので常にtrueを返すが、onSaveは呼ばれない
    }

    @Test
    fun `applyInlineWrap with different markers should work`() {
        val testCases =
            listOf(
                Triple("**", "**", "**text**"),
                Triple("*", "*", "*text*"),
                Triple("`", "`", "`text`"),
                Triple("~~", "~~", "~~text~~"),
                Triple("[", "](url)", "[text](url)")
            )

        for ((open, close, expected) in testCases) {
            val value = TextFieldValue("text", selection = TextRange(0, 4))
            val result = applyInlineWrap(open, close, value)
            assertEquals(expected, result.text, "Failed for markers: $open, $close")
        }
    }

    @Test
    fun `applyInlineWrap should handle Japanese text`() {
        val value = TextFieldValue("こんにちは世界", selection = TextRange(0, 7))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("**こんにちは世界**", result.text)
    }

    @Test
    fun `applyInlineWrap should handle partial selection`() {
        val value = TextFieldValue("Hello World Test", selection = TextRange(0, 5))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("**Hello** World Test", result.text)
        assertEquals(2, result.selection.start)
        assertEquals(7, result.selection.end)
    }

    @Test
    fun `applyInlineWrap should handle cursor at start`() {
        val value = TextFieldValue("Hello World", selection = TextRange(0))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("****Hello World", result.text)
        assertEquals(2, result.selection.start)
    }

    @Test
    fun `applyInlineWrap should handle cursor at end`() {
        val value = TextFieldValue("Hello World", selection = TextRange(11))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("Hello World****", result.text)
        assertEquals(13, result.selection.start)
    }

    @Test
    fun `applyInlineWrap should handle selection with newlines`() {
        val value = TextFieldValue("Hello\nWorld", selection = TextRange(0, 11))

        val result = applyInlineWrap("**", "**", value)

        assertEquals("**Hello\nWorld**", result.text)
        assertEquals(2, result.selection.start)
        assertEquals(13, result.selection.end)
    }
}
