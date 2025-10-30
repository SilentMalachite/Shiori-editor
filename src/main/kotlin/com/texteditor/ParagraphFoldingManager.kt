package com.texteditor

/** 段落折りたたみ機能を管理するクラス Ctrl + Enter + マウスクリックで段落を展開/折りたたみ */
class ParagraphFoldingManager {

    private val foldedParagraphs = mutableSetOf<Int>()
    private val foldedContent = mutableMapOf<Int, String>()
    private var enterPressed = false

    companion object {
        private const val FOLD_MARKER = " [...]"
    }

    /** Enterキーが押されたかどうかを設定 */
    fun setEnterPressed(pressed: Boolean) {
        enterPressed = pressed
    }

    /** Enterキーが押されているかどうか */
    fun isEnterPressed(): Boolean = enterPressed

    /** 段落を折りたたむ */
    fun foldParagraph(paragraphIndex: Int, text: String): String {
        if (foldedParagraphs.contains(paragraphIndex)) {
            return text // 既に折りたたまれている
        }

        val lines = text.lines()
        if (paragraphIndex >= lines.size) {
            return text
        }

        val range = getParagraphRange(paragraphIndex, lines)
        if (range.endParagraph <= range.startParagraph) {
            return text // 折りたたむコンテンツがない
        }

        // 折りたたむコンテンツを保存
        val foldedText =
            lines.subList(range.startParagraph + 1, range.endParagraph + 1).joinToString("\n")
        foldedContent[paragraphIndex] = foldedText
        foldedParagraphs.add(paragraphIndex)

        // 折りたたみ表示を生成
        val result = mutableListOf<String>()
        result.addAll(lines.subList(0, range.startParagraph + 1))
        result[range.startParagraph] += FOLD_MARKER
        result.addAll(lines.subList(range.endParagraph + 1, lines.size))

        return result.joinToString("\n")
    }

    /** 段落を展開する */
    fun expandParagraph(paragraphIndex: Int, text: String): String {
        if (!foldedParagraphs.contains(paragraphIndex)) {
            return text // 折りたたまれていない
        }

        val content = foldedContent[paragraphIndex] ?: return text
        val lines = text.lines()

        if (paragraphIndex >= lines.size) {
            return text
        }

        // 折りたたみマーカーを削除
        val lineWithMarker = lines[paragraphIndex]
        val lineWithoutMarker = lineWithMarker.replace(FOLD_MARKER, "")

        // 折りたたまれたコンテンツを挿入
        val result = mutableListOf<String>()
        result.addAll(lines.subList(0, paragraphIndex))
        result.add(lineWithoutMarker)
        result.add(content)
        result.addAll(lines.subList(paragraphIndex + 1, lines.size))

        // 折りたたみ状態をクリア
        foldedParagraphs.remove(paragraphIndex)
        foldedContent.remove(paragraphIndex)

        return result.joinToString("\n")
    }

    /** 段落の範囲を取得 */
    private fun getParagraphRange(startParagraph: Int, lines: List<String>): ParagraphRange {
        val range = ParagraphRange(startParagraph, startParagraph)

        if (startParagraph >= lines.size) {
            return range
        }

        val startText = lines[startParagraph].trim()

        // 見出しの場合、次の上位レベルの見出しまで
        if (startText.startsWith("#")) {
            val headerLevel = getHeaderLevel(startText)
            for (i in startParagraph + 1 until lines.size) {
                val text = lines[i].trim()
                if (text.startsWith("#") && getHeaderLevel(text) <= headerLevel) {
                    break
                }
                range.endParagraph = i
            }
        }
        // リスト項目の場合、連続するリスト項目まで
        else if (isListItem(startText)) {
            for (i in startParagraph + 1 until lines.size) {
                val text = lines[i].trim()
                if (!isListItem(text) && text.isNotEmpty()) {
                    break
                }
                if (text.isNotEmpty()) {
                    range.endParagraph = i
                }
            }
        }
        // その他の場合、次の空行または構造要素まで
        else {
            for (i in startParagraph + 1 until lines.size) {
                val text = lines[i].trim()
                if (
                    text.isEmpty() ||
                        text.startsWith("#") ||
                        isListItem(text) ||
                        text.startsWith(">")
                ) {
                    break
                }
                range.endParagraph = i
            }
            // 少なくとも1行は含める
            if (range.endParagraph == range.startParagraph) {
                range.endParagraph = minOf(range.startParagraph + 1, lines.size - 1)
            }
        }

        return range
    }

    /** 見出しレベルを取得 */
    private fun getHeaderLevel(text: String): Int {
        var level = 0
        for (c in text) {
            if (c == '#') {
                level++
            } else {
                break
            }
        }
        return level
    }

    /** リスト項目かどうかを判定 */
    private fun isListItem(text: String): Boolean {
        return text.matches(Regex("^\\s*[-*+]\\s+.*")) || text.matches(Regex("^\\s*\\d+\\.\\s+.*"))
    }

    /** 段落が折りたたまれているかどうか */
    fun isFolded(paragraphIndex: Int): Boolean {
        return foldedParagraphs.contains(paragraphIndex)
    }

    /** すべての折りたたみ状態をクリア */
    fun clearAllFolding() {
        foldedParagraphs.clear()
        foldedContent.clear()
    }

    /** 折りたたまれている段落のリストを取得 */
    fun getFoldedParagraphs(): Set<Int> {
        return foldedParagraphs.toSet()
    }

    /** 段落範囲を表すデータクラス */
    private data class ParagraphRange(var startParagraph: Int, var endParagraph: Int)
}
