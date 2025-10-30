package com.texteditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/** Markdown対応日本語テキストエディタのメインクラス */
fun startTextEditor() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
    val themeManager = remember { ThemeManager() }
    val languageSupport = remember { ProgrammingLanguageSupport() }
    val foldingManager = remember { ParagraphFoldingManager() }

    var text by remember { mutableStateOf(getDefaultInitialText()) }
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
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    // ツールバー
                    ToolbarSection(
                        colors = colors,
                        onNewClick = {
                            if (
                                confirmSaveIfModified(isModified) {
                                    doSave(currentFile, text) { currentFile = it }
                                }
                            ) {
                                text = getDefaultInitialText()
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
                                    text = content
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
                            applyInlineWrap("**", "**", text) {
                                text = it
                                isModified = true
                            }
                        },
                        onItalicClick = {
                            applyInlineWrap("*", "*", text) {
                                text = it
                                isModified = true
                            }
                        },
                        onCodeClick = {
                            applyInlineWrap("`", "`", text) {
                                text = it
                                isModified = true
                            }
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
                            text = text,
                            onTextChange = {
                                text = it
                                isModified = true
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
                                text = text,
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
    text: String,
    onTextChange: (String) -> Unit,
    colors: ThemeColors,
    languageSupport: ProgrammingLanguageSupport,
    darkMode: Boolean,
    modifier: Modifier = Modifier
) {
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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // エディタ
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                textStyle =
                    TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = colors.text
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
                    font-family: -apple-system, "Segoe UI", Roboto, "Noto Sans JP", sans-serif;
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
            </style>
        </head>
        <body>
            $htmlContent
        </body>
        </html>
        """
                .trimIndent()
        }

    // Compose DesktopにはWebViewコンポーネントがないため、テキスト表示で代替
    // 実際の実装では、JavaFXのWebViewを統合するか、Compose for Webを使用
    Column(
        modifier =
            modifier
                .background(
                    if (colors == DarkThemeColors) DarkThemeColors.background
                    else LightThemeColors.background
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
    ) {
        Text(
            text = "プレビュー機能はHTMLレンダリングが必要です。\n\nレンダリングされたHTML:\n\n$html",
            color = if (colors == DarkThemeColors) DarkThemeColors.text else LightThemeColors.text,
            fontSize = 12.sp
        )
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

fun doSave(currentFile: Path?, text: String, onSaved: (Path?) -> Unit): Boolean {
    if (currentFile == null) {
        return false
    }
    return ErrorHandler.safeExecute(
        {
            Files.writeString(currentFile, text, StandardCharsets.UTF_8)
            onSaved(currentFile)
        },
        "保存に失敗しました",
        "ファイルの保存中にエラーが発生しました"
    )
}

fun saveAsFile(text: String, onSaved: (Path) -> Unit) {
    val chooser = JFileChooser()
    chooser.fileFilter = FileNameExtensionFilter("Markdown (*.md)", "md")
    chooser.addChoosableFileFilter(FileNameExtensionFilter("テキスト (*.txt)", "txt"))
    chooser.addChoosableFileFilter(FileNameExtensionFilter("すべてのファイル (*.*)", "*"))
    val result = chooser.showSaveDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        val file = chooser.selectedFile.toPath()
        ErrorHandler.safeExecute(
            {
                Files.writeString(file, text, StandardCharsets.UTF_8)
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

fun applyInlineWrap(open: String, close: String, text: String, onTextChange: (String) -> Unit) {
    // 簡易実装（実際には選択範囲を処理）
    val newText = text + open + close
    onTextChange(newText)
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
