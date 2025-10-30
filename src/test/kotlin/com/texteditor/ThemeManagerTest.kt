package com.texteditor

import kotlin.test.*

/** ThemeManager のユニットテスト */
class ThemeManagerTest {

    @Test
    fun `initial theme should be light mode`() {
        val themeManager = ThemeManager()

        assertFalse(themeManager.isDarkMode())
    }

    @Test
    fun `toggleTheme should switch from light to dark`() {
        val themeManager = ThemeManager()

        assertFalse(themeManager.isDarkMode())

        themeManager.toggleTheme()

        assertTrue(themeManager.isDarkMode())
    }

    @Test
    fun `toggleTheme should switch from dark to light`() {
        val themeManager = ThemeManager()

        themeManager.toggleTheme() // Switch to dark
        assertTrue(themeManager.isDarkMode())

        themeManager.toggleTheme() // Switch back to light
        assertFalse(themeManager.isDarkMode())
    }

    @Test
    fun `multiple toggleTheme calls should alternate correctly`() {
        val themeManager = ThemeManager()

        // Initial: light
        assertFalse(themeManager.isDarkMode())

        // Toggle 1: dark
        themeManager.toggleTheme()
        assertTrue(themeManager.isDarkMode())

        // Toggle 2: light
        themeManager.toggleTheme()
        assertFalse(themeManager.isDarkMode())

        // Toggle 3: dark
        themeManager.toggleTheme()
        assertTrue(themeManager.isDarkMode())

        // Toggle 4: light
        themeManager.toggleTheme()
        assertFalse(themeManager.isDarkMode())
    }

    @Test
    fun `multiple ThemeManager instances should be independent`() {
        val themeManager1 = ThemeManager()
        val themeManager2 = ThemeManager()

        themeManager1.toggleTheme()

        assertTrue(themeManager1.isDarkMode())
        assertFalse(themeManager2.isDarkMode())
    }
}
