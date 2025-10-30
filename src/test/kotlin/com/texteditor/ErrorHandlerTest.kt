package com.texteditor

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.*

/** ErrorHandler のユニットテスト */
class ErrorHandlerTest {

    private lateinit var originalOut: PrintStream
    private lateinit var originalErr: PrintStream
    private lateinit var outCapture: ByteArrayOutputStream
    private lateinit var errCapture: ByteArrayOutputStream

    @BeforeTest
    fun setup() {
        // 標準出力とエラー出力をキャプチャ
        originalOut = System.out
        originalErr = System.err
        outCapture = ByteArrayOutputStream()
        errCapture = ByteArrayOutputStream()
        System.setOut(PrintStream(outCapture))
        System.setErr(PrintStream(errCapture))
    }

    @AfterTest
    fun tearDown() {
        // 標準出力とエラー出力を復元
        System.setOut(originalOut)
        System.setErr(originalErr)
    }

    @Test
    fun `logError should output error message with throwable`() {
        val testException = RuntimeException("Test exception")
        ErrorHandler.logError("Test error message", testException)

        val errOutput = errCapture.toString()
        assertTrue(errOutput.contains("[ERROR] Test error message"))
        assertTrue(errOutput.contains("Test exception"))
    }

    @Test
    fun `logError should output error message without throwable`() {
        ErrorHandler.logError("Test error message", null)

        val outOutput = outCapture.toString()
        assertTrue(outOutput.contains("[ERROR] Test error message"))
    }

    @Test
    fun `logWarning should output warning message`() {
        ErrorHandler.logWarning("Test warning message")

        val outOutput = outCapture.toString()
        assertTrue(outOutput.contains("[WARN] Test warning message"))
    }

    @Test
    fun `logInfo should output info message`() {
        ErrorHandler.logInfo("Test info message")

        val outOutput = outCapture.toString()
        assertTrue(outOutput.contains("[INFO] Test info message"))
    }

    @Test
    fun `safeExecute with errorHandler should return true on success`() {
        var executed = false
        val result =
            ErrorHandler.safeExecute(
                operation = { executed = true },
                errorHandler = { fail("Error handler should not be called") }
            )

        assertTrue(result)
        assertTrue(executed)
    }

    @Test
    fun `safeExecute with errorHandler should return false on exception`() {
        var errorHandlerCalled = false
        val testException = RuntimeException("Test exception")

        val result =
            ErrorHandler.safeExecute(
                operation = { throw testException },
                errorHandler = { e ->
                    errorHandlerCalled = true
                    assertEquals(testException, e)
                }
            )

        assertFalse(result)
        assertTrue(errorHandlerCalled)
    }

    @Test
    fun `safeExecute with error title should return true on success`() {
        var executed = false
        val result =
            ErrorHandler.safeExecute(
                operation = { executed = true },
                errorTitle = "Test Title",
                errorMessage = "Test Message"
            )

        assertTrue(result)
        assertTrue(executed)
    }

    @Test
    fun `safeExecute with error title should return false on exception and log error`() {
        val result =
            ErrorHandler.safeExecute(
                operation = { throw RuntimeException("Test exception") },
                errorTitle = "Test Title",
                errorMessage = "Test Message"
            )

        assertFalse(result)

        val errOutput = errCapture.toString()
        assertTrue(errOutput.contains("[ERROR] Test Title: Test Message"))
        assertTrue(errOutput.contains("Test exception"))
    }

    @Test
    fun `safeExecute should handle different exception types`() {
        val exceptions =
            listOf(
                IllegalArgumentException("Illegal argument"),
                IllegalStateException("Illegal state"),
                NullPointerException("Null pointer")
            )

        for (exception in exceptions) {
            val result =
                ErrorHandler.safeExecute(
                    operation = { throw exception },
                    errorHandler = { e -> assertEquals(exception, e) }
                )
            assertFalse(result)
        }
    }
}
