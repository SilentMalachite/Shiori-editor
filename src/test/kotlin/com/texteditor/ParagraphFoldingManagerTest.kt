package com.texteditor

import kotlin.test.*

/** ParagraphFoldingManager のユニットテスト */
class ParagraphFoldingManagerTest {

    private lateinit var manager: ParagraphFoldingManager

    @BeforeTest
    fun setup() {
        manager = ParagraphFoldingManager()
    }

    @Test
    fun `foldParagraph should fold a simple paragraph`() {
        val text =
            """
            First line
            Second line
            Third line
            """
                .trimIndent()

        val result = manager.foldParagraph(0, text)

        assertTrue(result.contains("[...]"))
        assertTrue(manager.isFolded(0))
    }

    @Test
    fun `expandParagraph should restore folded content`() {
        val text =
            """
            First line
            Second line
            Third line
            """
                .trimIndent()

        val folded = manager.foldParagraph(0, text)
        val expanded = manager.expandParagraph(0, folded)

        assertFalse(manager.isFolded(0))
        assertFalse(expanded.contains("[...]"))
    }

    @Test
    fun `foldParagraph with heading should fold until next heading`() {
        val text =
            """
            # Heading 1
            Content under heading 1
            More content
            ## Heading 2
            Content under heading 2
            """
                .trimIndent()

        val result = manager.foldParagraph(0, text)

        assertTrue(result.contains("[...]"))
        assertTrue(manager.isFolded(0))
    }

    @Test
    fun `foldParagraph with list should fold list items`() {
        val text =
            """
            - Item 1
            - Item 2
            - Item 3
            
            Next paragraph
            """
                .trimIndent()

        val result = manager.foldParagraph(0, text)

        assertTrue(result.contains("[...]"))
        assertTrue(manager.isFolded(0))
    }

    @Test
    fun `foldParagraph with numbered list should fold list items`() {
        val text =
            """
            1. First item
            2. Second item
            3. Third item
            
            Next paragraph
            """
                .trimIndent()

        val result = manager.foldParagraph(0, text)

        assertTrue(result.contains("[...]"))
        assertTrue(manager.isFolded(0))
    }

    @Test
    fun `foldParagraph should not fold already folded paragraph`() {
        val text =
            """
            First line
            Second line
            Third line
            """
                .trimIndent()

        val result1 = manager.foldParagraph(0, text)
        val result2 = manager.foldParagraph(0, result1)

        assertEquals(result1, result2)
    }

    @Test
    fun `expandParagraph should not affect unfolded paragraph`() {
        val text =
            """
            First line
            Second line
            Third line
            """
                .trimIndent()

        val result = manager.expandParagraph(0, text)

        assertEquals(text, result)
    }

    @Test
    fun `isFolded should return false for unfolded paragraph`() {
        assertFalse(manager.isFolded(0))
        assertFalse(manager.isFolded(5))
        assertFalse(manager.isFolded(100))
    }

    @Test
    fun `isFolded should return true for folded paragraph`() {
        val text =
            """
            First line
            Second line
            Third line
            """
                .trimIndent()

        manager.foldParagraph(0, text)

        assertTrue(manager.isFolded(0))
    }

    @Test
    fun `clearAllFolding should clear all folded paragraphs`() {
        val text =
            """
            # Section 1
            Content 1
            
            # Section 2
            Content 2
            """
                .trimIndent()

        manager.foldParagraph(0, text)
        manager.foldParagraph(3, text)

        assertTrue(manager.isFolded(0))
        assertTrue(manager.isFolded(3))

        manager.clearAllFolding()

        assertFalse(manager.isFolded(0))
        assertFalse(manager.isFolded(3))
    }

    @Test
    fun `getFoldedParagraphs should return set of folded paragraph indices`() {
        val text =
            """
            # Section 1
            Content 1
            
            # Section 2
            Content 2
            """
                .trimIndent()

        manager.foldParagraph(0, text)
        manager.foldParagraph(3, text)

        val folded = manager.getFoldedParagraphs()

        assertEquals(2, folded.size)
        assertTrue(folded.contains(0))
        assertTrue(folded.contains(3))
    }

    @Test
    fun `foldParagraph should handle out of bounds index`() {
        val text =
            """
            First line
            Second line
            """
                .trimIndent()

        val result = manager.foldParagraph(100, text)

        assertEquals(text, result)
        assertFalse(manager.isFolded(100))
    }

    @Test
    fun `expandParagraph should handle out of bounds index`() {
        val text =
            """
            First line
            Second line
            """
                .trimIndent()

        val result = manager.expandParagraph(100, text)

        assertEquals(text, result)
    }

    @Test
    fun `foldParagraph with markdown heading levels should respect hierarchy`() {
        val text =
            """
            # H1
            Content 1
            ## H2
            Content 2
            ### H3
            Content 3
            """
                .trimIndent()

        val result = manager.foldParagraph(0, text)

        assertTrue(result.contains("[...]"))
        assertTrue(manager.isFolded(0))
    }

    @Test
    fun `setEnterPressed and isEnterPressed should work correctly`() {
        assertFalse(manager.isEnterPressed())

        manager.setEnterPressed(true)
        assertTrue(manager.isEnterPressed())

        manager.setEnterPressed(false)
        assertFalse(manager.isEnterPressed())
    }

    @Test
    fun `foldParagraph with quote should fold quote block`() {
        val text =
            """
            > Quote line 1
            > Quote line 2
            
            Next paragraph
            """
                .trimIndent()

        val result = manager.foldParagraph(0, text)

        assertTrue(result.contains("[...]"))
    }

    @Test
    fun `multiple fold and expand operations should work correctly`() {
        val text =
            """
            # Section 1
            Content 1
            # Section 2
            Content 2
            """
                .trimIndent()

        // Fold first section
        val folded1 = manager.foldParagraph(0, text)
        assertTrue(manager.isFolded(0))

        // Expand first section
        val expanded1 = manager.expandParagraph(0, folded1)
        assertFalse(manager.isFolded(0))

        // Fold again
        manager.foldParagraph(0, expanded1)
        assertTrue(manager.isFolded(0))
    }
}
