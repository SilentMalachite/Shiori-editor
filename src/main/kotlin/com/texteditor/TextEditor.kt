package com.texteditor

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/** Markdown対応日本語テキストエディタのメインクラス */
fun startTextEditor() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
    val themeManager = remember { ThemeManager() }
    val languageSupport = remember { ProgrammingLanguageSupport() }
    val foldingManager = remember { ParagraphFoldingManager() }

    var text by remember { mutableStateOf(TextFieldValue(getDefaultInitialText())) }
    var currentFile by remember { mutableStateOf<Path?>(null) }
    var isModified by remember { mutableStateOf(false) }
    var previewEnabled by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(themeManager.isDarkMode()) }
    var selectedLanguage by remember { mutableStateOf("Markdown") }
    var ctrlEnterPressed by remember { mutableStateOf(false) }

    val mdParser = remember { Parser.builder().build() }
    val mdRenderer = remember { HtmlRenderer.builder().build() }

    val colors = if (darkMode) DarkThemeColors else LightThemeColors

    Window(
        onCloseRequest = {
            if (
                confirmSaveIfModified(isModified) {
                    if (doSave(currentFile, text) { path -> currentFile = path }) {
                        isModified = false
                    }
                }
            ) {
                exitApplication()
            }
        },
        title =
            if (isModified) "*"
            else "" + "Markdown対応日本語テキストエディタ" + (currentFile?.let { " - ${it.fileName}" } ?: ""),
        state = windowState
    ) {
        // Ctrl/⌘ + Enter を検知（次のクリックと組み合わせてフォールディング）
        MaterialTheme(
            colors =
                if (darkMode) {
                    darkColors(
                        primary = DarkThemeColors.link,
                        background = DarkThemeColors.background,
                        surface = DarkThemeColors.surface,
                        onPrimary = Color.White,
                        onBackground = DarkThemeColors.text,
                        onSurface = DarkThemeColors.text
                    )
                } else {
                    lightColors(
                        primary = LightThemeColors.link,
                        background = LightThemeColors.background,
                        surface = LightThemeColors.surface,
                        onPrimary = Color.White,
                        onBackground = LightThemeColors.text,
                        onSurface = LightThemeColors.text
                    )
                }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Markdown対応日本語テキストエディタ") },
                        backgroundColor =
                            if (darkMode) DarkThemeColors.surface else LightThemeColors.surface,
                        contentColor = if (darkMode) DarkThemeColors.text else LightThemeColors.text
                    )
                }
            ) { padding ->
                Column(
                    modifier =
                        Modifier.fillMaxSize().padding(padding).onPreviewKeyEvent { event ->
                            if (
                                event.type == KeyEventType.KeyDown &&
                                    event.key == Key.Enter &&
                                    (event.isCtrlPressed || event.isMetaPressed)
                            ) {
                                ctrlEnterPressed = true
                                true
                            } else {
                                false
                            }
                        }
                ) {
                    // ツールバー
                    ToolbarSection(
                        colors = colors,
                        onNewClick = {
                            if (
                                confirmSaveIfModified(isModified) {
                                    doSave(currentFile, text) { currentFile = it }
                                }
                            ) {
                                text = TextFieldValue(getDefaultInitialText())
                                currentFile = null
                                isModified = false
                            }
                        },
                        onOpenClick = {
                            if (
                                confirmSaveIfModified(isModified) {
                                    doSave(currentFile, text) { currentFile = it }
                                }
                            ) {
                                openFile { file, content ->
                                    currentFile = file
                                    text = TextFieldValue(content)
                                    isModified = false
                                }
                            }
                        },
                        onSaveClick = {
                            if (currentFile == null) {
                                saveAsFile(text) { file ->
                                    currentFile = file
                                    isModified = false
                                }
                            } else {
                                doSave(currentFile, text) {}
                                isModified = false
                            }
                        },
                        onBoldClick = {
                            text = applyInlineWrap("**", "**", text)
                            isModified = true
                        },
                        onItalicClick = {
                            text = applyInlineWrap("*", "*", text)
                            isModified = true
                        },
                        onCodeClick = {
                            text = applyInlineWrap("`", "`", text)
                            isModified = true
                        },
                        selectedLanguage = selectedLanguage,
                        onLanguageChanged = { lang ->
                            selectedLanguage = lang
                            when (lang) {
                                "Java" -> languageSupport.setLanguage(Language.JAVA)
                                "Go" -> languageSupport.setLanguage(Language.GO)
                                "C/C++" -> languageSupport.setLanguage(Language.CPP)
                                "Haskell" -> languageSupport.setLanguage(Language.HASKELL)
                                "Markdown" -> languageSupport.setLanguage(Language.MARKDOWN)
                                else -> languageSupport.setLanguage(Language.PLAIN_TEXT)
                            }
                        }
                    )

                    // メニューバー
                    MenuBarSection(
                        colors = colors,
                        darkMode = darkMode,
                        onDarkModeToggle = {
                            darkMode = !darkMode
                            themeManager.toggleTheme()
                        },
                        previewEnabled = previewEnabled,
                        onPreviewToggle = { previewEnabled = !previewEnabled }
                    )

                    // エディタエリア
                    Row(modifier = Modifier.fillMaxSize()) {
                        // エディタ
                        EditorSection(
                            value = text,
                            onValueChange = {
                                text = it
                                isModified = true
                            },
                            onToggleFoldAtLine = { lineIndex ->
                                if (ctrlEnterPressed) {
                                    val original = text.text
                                    val newText =
                                        if (foldingManager.isFolded(lineIndex)) {
                                            foldingManager.expandParagraph(lineIndex, original)
                                        } else {
                                            foldingManager.foldParagraph(lineIndex, original)
                                        }
                                    text =
                                        text.copy(
                                            text = newText,
                                            selection = TextRange(newText.length)
                                        )
                                    ctrlEnterPressed = false
                                    isModified = true
                                }
                            },
                            colors = colors,
                            languageSupport = languageSupport,
                            darkMode = darkMode,
                            modifier =
                                Modifier.weight(if (previewEnabled) 0.5f else 1f).fillMaxHeight()
                        )

                        // プレビュー
                        if (previewEnabled) {
                            PreviewSection(
                                text = text.text,
                                mdParser = mdParser,
                                mdRenderer = mdRenderer,
                                colors = colors,
                                modifier = Modifier.weight(0.5f).fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolbarSection(
    colors: ThemeColors,
    onNewClick: () -> Unit,
    onOpenClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onCodeClick: () -> Unit,
    selectedLanguage: String,
    onLanguageChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(colors.surface).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onNewClick) { Text("新規") }
        Button(onClick = onOpenClick) { Text("開く") }
        Button(onClick = onSaveClick) { Text("保存") }

        Divider(
            modifier = Modifier.height(24.dp),
            color = if (colors == DarkThemeColors) DarkThemeColors.text else LightThemeColors.text
        )

        IconButton(onClick = onBoldClick) { Text("B", fontWeight = FontWeight.Bold) }
        IconButton(onClick = onItalicClick) {
            Text("I", style = TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
        }
        Button(onClick = onCodeClick) { Text("Code") }

        Divider(
            modifier = Modifier.height(24.dp),
            color = if (colors == DarkThemeColors) DarkThemeColors.text else LightThemeColors.text
        )

        Text("言語:", modifier = Modifier.padding(end = 8.dp))
        val languages = listOf("Markdown", "Java", "Go", "C/C++", "Haskell", "プレーンテキスト")
        var expanded by remember { mutableStateOf(false) }
        Box {
            Button(onClick = { expanded = true }) { Text(selectedLanguage) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        onClick = {
                            onLanguageChanged(lang)
                            expanded = false
                        }
                    ) {
                        Text(lang)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuBarSection(
    colors: ThemeColors,
    darkMode: Boolean,
    onDarkModeToggle: () -> Unit,
    previewEnabled: Boolean,
    onPreviewToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(colors.surface).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("ファイル")
        Text("編集")
        Row {
            Text("表示")
            Checkbox(
                checked = darkMode,
                onCheckedChange = { onDarkModeToggle() },
                modifier = Modifier.padding(start = 8.dp)
            )
            Text("ダークモード", modifier = Modifier.padding(start = 4.dp))

            Checkbox(
                checked = previewEnabled,
                onCheckedChange = { onPreviewToggle() },
                modifier = Modifier.padding(start = 16.dp)
            )
            Text("Markdownプレビュー", modifier = Modifier.padding(start = 4.dp))
        }
        Text("ヘルプ")
    }
}

@Composable
fun EditorSection(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onToggleFoldAtLine: (Int) -> Unit,
    colors: ThemeColors,
    languageSupport: ProgrammingLanguageSupport,
    darkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val text = value.text
    val highlightedText =
        remember(text, languageSupport.getCurrentLanguage(), darkMode) {
            languageSupport.computeHighlighting(text, darkMode)
        }

    Column(modifier = modifier) {
        // 行番号とエディタ
        Row(modifier = Modifier.fillMaxSize()) {
            // 行番号
            Column(
                modifier =
                    Modifier.width(40.dp)
                        .fillMaxHeight()
                        .background(colors.surface)
                        .verticalScroll(rememberScrollState())
            ) {
                text.lines().forEachIndexed { index, _ ->
                    Text(
                        text = "${index + 1}",
                        fontSize = 12.sp,
                        color = colors.text.copy(alpha = 0.6f),
                        modifier =
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp).pointerInput(
                                Unit
                            ) {
                                detectTapGestures(onTap = { onToggleFoldAtLine(index) })
                            }
                    )
                }
            }
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier =
                    Modifier.weight(1f).fillMaxHeight().onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Tab) {
                            val newVal =
                                if (event.isShiftPressed) unindentSelection(value)
                                else indentSelection(value)
                            if (newVal != null) {
                                onValueChange(newVal)
                                true
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    },
                textStyle =
                    TextStyle(
                        fontFamily = preferredFontFamily(languageSupport.getCurrentLanguage()),
                        fontSize = 14.sp,
                        color = colors.text,
                        localeList = LocaleList(Locale("ja-JP"))
                    ),
                colors =
                    TextFieldDefaults.textFieldColors(
                        backgroundColor = colors.background,
                        textColor = colors.text,
                        cursorColor = colors.text,
                        focusedIndicatorColor = colors.link,
                        unfocusedIndicatorColor = colors.text.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@Composable
fun PreviewSection(
    text: String,
    mdParser: Parser,
    mdRenderer: HtmlRenderer,
    colors: ThemeColors,
    modifier: Modifier = Modifier
) {
    val html =
        remember(text) {
            val document = mdParser.parse(text)
            val htmlContent = mdRenderer.render(document)
            """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body {
                    font-family: -apple-system, "Yu Gothic UI", Meiryo, "MS Gothic", "Noto Sans JP", "Segoe UI", Roboto, sans-serif;
                    padding: 12px;
                    background-color: ${if (colors == DarkThemeColors) "#1e1e1e" else "#ffffff"};
                    color: ${if (colors == DarkThemeColors) "#f0f0f0" else "#323130"};
                }
                pre, code {
                    font-family: Consolas, "Courier New", monospace;
                    background-color: ${if (colors == DarkThemeColors) "#3a3a3a" else "#f6f8fa"};
                    padding: 4px;
                    border-radius: 4px;
                }
                pre {
                    padding: 12px;
                    overflow-x: auto;
                }
                a { color: ${colors.link.toArgbCss()}; }
            </style>
        </head>
        <body>
            $htmlContent
        </body>
        </html>
        """
                .trimIndent()
        }

    HtmlWebView(html = html, modifier = modifier)
}

@Composable
private fun HtmlWebView(html: String, modifier: Modifier = Modifier) {
    // JavaFX 初期化
    remember { runCatching { Platform.startup {} } }
    SwingPanel(
        factory = {
            val panel = JFXPanel()
            Platform.runLater {
                val webView = WebView()
                val engine = webView.engine
                engine.loadContent(html)
                panel.scene = Scene(webView)
            }
            panel
        },
        update = { panel ->
            Platform.runLater {
                val webView =
                    (panel.scene?.root as? WebView)
                        ?: run {
                            val wv = WebView()
                            panel.scene = Scene(wv)
                            wv
                        }
                webView.engine.loadContent(html)
            }
        },
        modifier =
            modifier.background(
                if (html.contains("#1e1e1e")) DarkThemeColors.background
                else LightThemeColors.background
            )
    )
}

private fun Color.toArgbCss(): String {
    // Convert Compose Color (0..1) to CSS hex
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return "#%02x%02x%02x".format(r, g, b)
}

private fun preferredFontFamily(lang: Language): FontFamily {
    return when (lang) {
        Language.MARKDOWN,
        Language.PLAIN_TEXT -> FontFamily.SansSerif
        else -> FontFamily.Monospace
    }
}

// ファイル操作関数
fun openFile(onFileLoaded: (Path, String) -> Unit) {
    val chooser = JFileChooser()
    chooser.fileFilter = FileNameExtensionFilter("テキスト/Markdown", "txt", "md", "markdown")
    val result = chooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        val file = chooser.selectedFile.toPath()
        ErrorHandler.safeExecute(
            {
                val content = Files.readString(file, StandardCharsets.UTF_8)
                onFileLoaded(file, content)
            },
            "ファイルを開けませんでした",
            "ファイルの読み込み中にエラーが発生しました"
        )
    }
}

fun doSave(currentFile: Path?, text: TextFieldValue, onSaved: (Path?) -> Unit): Boolean {
    if (currentFile == null) {
        return false
    }
    return ErrorHandler.safeExecute(
        {
            Files.writeString(currentFile, text.text, StandardCharsets.UTF_8)
            onSaved(currentFile)
        },
        "保存に失敗しました",
        "ファイルの保存中にエラーが発生しました"
    )
}

fun saveAsFile(text: TextFieldValue, onSaved: (Path) -> Unit) {
    val chooser = JFileChooser()
    chooser.fileFilter = FileNameExtensionFilter("Markdown (*.md)", "md")
    chooser.addChoosableFileFilter(FileNameExtensionFilter("テキスト (*.txt)", "txt"))
    chooser.addChoosableFileFilter(FileNameExtensionFilter("すべてのファイル (*.*)", "*"))
    val result = chooser.showSaveDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        val file = chooser.selectedFile.toPath()
        ErrorHandler.safeExecute(
            {
                Files.writeString(file, text.text, StandardCharsets.UTF_8)
                onSaved(file)
            },
            "保存に失敗しました",
            "ファイルの保存中にエラーが発生しました"
        )
    }
}

fun confirmSaveIfModified(isModified: Boolean, onSave: () -> Unit): Boolean {
    if (!isModified) return true
    // 簡易実装（実際にはダイアログを表示）
    return true
}

fun applyInlineWrap(open: String, close: String, value: TextFieldValue): TextFieldValue {
    val text = value.text
    val selStart = value.selection.start.coerceIn(0, text.length)
    val selEnd = value.selection.end.coerceIn(0, text.length)
    val start = minOf(selStart, selEnd)
    val end = maxOf(selStart, selEnd)
    val selected = text.substring(start, end)
    val newText = text.substring(0, start) + open + selected + close + text.substring(end)
    val newCursor =
        if (selected.isEmpty()) start + open.length else start + open.length + selected.length
    val newSel =
        if (selected.isEmpty()) TextRange(newCursor)
        else TextRange(start + open.length, start + open.length + selected.length)
    return value.copy(text = newText, selection = newSel)
}

fun getDefaultInitialText(): String {
    return """# Markdown対応日本語テキストエディタ

このエディタは以下の機能に対応しています：

## 基本機能
- **日本語完全サポート**
- *Markdown構文ハイライト*
- `コードハイライト`
- 段落の折りたたみ
- ダーク/ライトテーマ切り替え

## プログラミング言語サポート
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("こんにちは、世界！");
    }
}
```

### 注意事項
> このエディタはUTF-8エンコーディングに完全対応しています。

ファイルの作成と編集を開始してください！

### 使用方法
- **段落折りたたみ**: Ctrl + Enter を押しながらマウスクリックで段落を折りたたみ/展開
- **テーマ切り替え**: 表示メニューからダークモードを選択
- **言語切り替え**: ツールバーの言語コンボボックスから選択"""
}

private fun indentSelection(value: TextFieldValue): TextFieldValue? {
    val text = value.text
    val selStart = value.selection.start
    val selEnd = value.selection.end
    if (selStart < 0 || selEnd < 0) return null
    val startLineStart =
        text.lastIndexOf('\n', startIndex = (minOf(selStart, selEnd) - 1).coerceAtLeast(0)) + 1
    val endLineEndIdx =
        text.indexOf('\n', startIndex = maxOf(selStart, selEnd)).let {
            if (it == -1) text.length else it
        }
    val segment = text.substring(startLineStart, endLineEndIdx)
    val indented = segment.lines().joinToString("\n") { "    " + it }
    val newText = text.substring(0, startLineStart) + indented + text.substring(endLineEndIdx)
    val deltaPerLine = 4
    val linesAffected = segment.count { it == '\n' } + 1
    val newStart = value.selection.start + deltaPerLine
    val newEnd = value.selection.end + deltaPerLine * linesAffected
    return value.copy(text = newText, selection = TextRange(newStart, newEnd))
}

private fun unindentSelection(value: TextFieldValue): TextFieldValue? {
    val text = value.text
    val selStart = value.selection.start
    val selEnd = value.selection.end
    if (selStart < 0 || selEnd < 0) return null
    val startLineStart =
        text.lastIndexOf('\n', startIndex = (minOf(selStart, selEnd) - 1).coerceAtLeast(0)) + 1
    val endLineEndIdx =
        text.indexOf('\n', startIndex = maxOf(selStart, selEnd)).let {
            if (it == -1) text.length else it
        }
    val segment = text.substring(startLineStart, endLineEndIdx)
    var removedTotal = 0
    val unindented =
        segment.lines().joinToString("\n") {
            val removed =
                when {
                    it.startsWith("\t") -> 1
                    it.startsWith("    ") -> 4
                    it.startsWith("   ") -> 3
                    it.startsWith("  ") -> 2
                    it.startsWith(" ") -> 1
                    else -> 0
                }
            removedTotal += removed
            it.drop(removed)
        }
    val newText = text.substring(0, startLineStart) + unindented + text.substring(endLineEndIdx)
    val newStart =
        (value.selection.start - 1).let {
            if (it < startLineStart) value.selection.start
            else value.selection.start - minOf(4, value.selection.start - startLineStart)
        }
    val newEnd = value.selection.end - removedTotal
    return value.copy(
        text = newText,
        selection = TextRange(newStart.coerceAtLeast(0), newEnd.coerceAtLeast(0))
    )
}
