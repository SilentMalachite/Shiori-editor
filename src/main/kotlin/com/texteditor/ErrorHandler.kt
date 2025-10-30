package com.texteditor

/** アプリケーション全体のエラーハンドリングを管理するクラス */
object ErrorHandler {

    /** ログ出力用のメソッド（将来的にログフレームワークに置き換え可能） */
    private fun log(level: String, message: String, throwable: Throwable?) {
        val logMessage = "[$level] $message"
        if (throwable != null) {
            System.err.println(logMessage)
            throwable.printStackTrace()
        } else {
            println(logMessage)
        }
    }

    /** エラーログを出力 */
    fun logError(message: String, throwable: Throwable?) {
        log("ERROR", message, throwable)
    }

    /** 警告ログを出力 */
    fun logWarning(message: String) {
        log("WARN", message, null)
    }

    /** 情報ログを出力 */
    fun logInfo(message: String) {
        log("INFO", message, null)
    }

    /** 例外を安全に実行し、エラーが発生した場合はカスタムハンドラーを呼び出す */
    fun safeExecute(operation: () -> Unit, errorHandler: (Exception) -> Unit): Boolean {
        return try {
            operation()
            true
        } catch (e: Exception) {
            errorHandler(e)
            false
        }
    }

    /** 例外を安全に実行し、エラーが発生した場合はログに記録する */
    fun safeExecute(operation: () -> Unit, errorTitle: String, errorMessage: String): Boolean {
        return try {
            operation()
            true
        } catch (e: Exception) {
            logError("$errorTitle: $errorMessage", e)
            false
        }
    }
}
