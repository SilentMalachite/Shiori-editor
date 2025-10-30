package com.texteditor

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.*

/** MarkdownHighlighter のユニットテスト */
class MarkdownHighlighterTest {

    private lateinit var highlighter: MarkdownHighlighter

    @BeforeTest
    fun setup() {
        highlighter = MarkdownHighlighter()
    }

    @Test
    fun `computeHighlighting should return AnnotatedString`() {
        val text = "Plain text"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertTrue(result is AnnotatedString)
        assertEquals(text, result.text)
    }

    @Test
    fun `computeHighlighting should highlight heading1`() {
        val text = "# Heading 1"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight heading2`() {
        val text = "## Heading 2"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight heading3`() {
        val text = "### Heading 3"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight bold text`() {
        val text = "This is **bold** text"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight italic text`() {
        val text = "This is *italic* text"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight inline code`() {
        val text = "This is `inline code` text"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight code block`() {
        val text = "```\ncode block\n```"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight quote`() {
        val text = "> This is a quote"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight link`() {
        val text = "[Link Text](https://example.com)"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight image`() {
        val text = "![Alt Text](image.png)"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight unordered list`() {
        val text = "- List item"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight ordered list`() {
        val text = "1. First item"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight strikethrough`() {
        val text = "This is ~~strikethrough~~ text"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight horizontal rule`() {
        val text = "---"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should work in dark mode`() {
        val text = "# Heading"
        val lightResult = highlighter.computeHighlighting(text, isDarkMode = false)
        val darkResult = highlighter.computeHighlighting(text, isDarkMode = true)

        assertNotNull(lightResult)
        assertNotNull(darkResult)
        assertEquals(lightResult.text, darkResult.text)
        assertTrue(lightResult.spanStyles.isNotEmpty())
        assertTrue(darkResult.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should handle empty text`() {
        val result = highlighter.computeHighlighting("", isDarkMode = false)

        assertNotNull(result)
        assertEquals("", result.text)
    }

    @Test
    fun `computeHighlighting should handle complex markdown`() {
        val text =
            """
            # Main Heading
            
            This is a paragraph with **bold** and *italic* text.
            
            ## Sub Heading
            
            - Item 1
            - Item 2
            
            > A quote
            
            `inline code`
            
            [Link](https://example.com)
            """
                .trimIndent()

        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should handle multiple bold in one line`() {
        val text = "**bold1** and **bold2**"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should handle nested markdown syntax`() {
        val text = "# Heading with **bold** text"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should handle multiline quote`() {
        val text = "> Line 1\n> Line 2"
        val result = highlighter.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should handle different heading levels`() {
        val headings = listOf("# H1", "## H2", "### H3", "#### H4", "##### H5", "###### H6")

        for (heading in headings) {
            val result = highlighter.computeHighlighting(heading, isDarkMode = false)
            assertNotNull(result)
            assertEquals(heading, result.text)
            assertTrue(result.spanStyles.isNotEmpty(), "Failed for: $heading")
        }
    }
}
