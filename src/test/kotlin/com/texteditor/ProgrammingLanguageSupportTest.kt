package com.texteditor

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.*

/** ProgrammingLanguageSupport のユニットテスト */
class ProgrammingLanguageSupportTest {

    private lateinit var support: ProgrammingLanguageSupport

    @BeforeTest
    fun setup() {
        support = ProgrammingLanguageSupport()
    }

    @Test
    fun `initial language should be Markdown`() {
        assertEquals(Language.MARKDOWN, support.getCurrentLanguage())
    }

    @Test
    fun `setLanguage should change current language`() {
        support.setLanguage(Language.JAVA)
        assertEquals(Language.JAVA, support.getCurrentLanguage())

        support.setLanguage(Language.GO)
        assertEquals(Language.GO, support.getCurrentLanguage())

        support.setLanguage(Language.CPP)
        assertEquals(Language.CPP, support.getCurrentLanguage())

        support.setLanguage(Language.HASKELL)
        assertEquals(Language.HASKELL, support.getCurrentLanguage())

        support.setLanguage(Language.PLAIN_TEXT)
        assertEquals(Language.PLAIN_TEXT, support.getCurrentLanguage())
    }

    @Test
    fun `computeHighlighting should return AnnotatedString for plain text`() {
        support.setLanguage(Language.PLAIN_TEXT)

        val text = "This is plain text"
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertTrue(result is AnnotatedString)
        assertEquals(text, result.text)
    }

    @Test
    fun `computeHighlighting should highlight Java keywords`() {
        support.setLanguage(Language.JAVA)

        val text = "public class HelloWorld { }"
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight Java strings`() {
        support.setLanguage(Language.JAVA)

        val text = """String message = "Hello, World!";"""
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight Java comments`() {
        support.setLanguage(Language.JAVA)

        val text = "// This is a comment\nint x = 5;"
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight Go keywords`() {
        support.setLanguage(Language.GO)

        val text = "func main() { }"
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight Go strings with backticks`() {
        support.setLanguage(Language.GO)

        val text = "var message = `Hello, World!`"
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
    }

    @Test
    fun `computeHighlighting should highlight C++ keywords`() {
        support.setLanguage(Language.CPP)

        val text = "class MyClass { public: int x; };"
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight C++ preprocessor directives`() {
        support.setLanguage(Language.CPP)

        val text = "#include <iostream>"
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight Haskell keywords`() {
        support.setLanguage(Language.HASKELL)

        val text = "module Main where"
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should work in dark mode`() {
        support.setLanguage(Language.JAVA)

        val text = "public class Test { }"
        val lightResult = support.computeHighlighting(text, isDarkMode = false)
        val darkResult = support.computeHighlighting(text, isDarkMode = true)

        assertNotNull(lightResult)
        assertNotNull(darkResult)
        assertEquals(lightResult.text, darkResult.text)
    }

    @Test
    fun `computeHighlighting should handle empty text`() {
        support.setLanguage(Language.JAVA)

        val result = support.computeHighlighting("", isDarkMode = false)

        assertNotNull(result)
        assertEquals("", result.text)
    }

    @Test
    fun `computeHighlighting should handle multiline code`() {
        support.setLanguage(Language.JAVA)

        val text =
            """
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """
                .trimIndent()

        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should highlight numbers`() {
        support.setLanguage(Language.JAVA)

        val text = "int x = 42; double y = 3.14;"
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun `computeHighlighting should handle Markdown when language is set to Markdown`() {
        support.setLanguage(Language.MARKDOWN)

        val text = "# Hello World\n\nThis is **bold** text."
        val result = support.computeHighlighting(text, isDarkMode = false)

        assertNotNull(result)
        assertEquals(text, result.text)
    }

    @Test
    fun `Language enum should have all expected values`() {
        val languages =
            listOf(
                Language.JAVA,
                Language.GO,
                Language.CPP,
                Language.HASKELL,
                Language.MARKDOWN,
                Language.PLAIN_TEXT
            )

        assertEquals(6, languages.size)
        assertEquals(6, Language.values().size)
    }
}
