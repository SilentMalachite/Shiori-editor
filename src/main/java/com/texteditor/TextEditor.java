package com.texteditor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown対応日本語テキストエディタのメインクラス
 */
public class TextEditor extends Application {
    
    private CodeArea codeArea;
    private ThemeManager themeManager;
    private MarkdownHighlighter markdownHighlighter;
    private ParagraphFoldingManager foldingManager;
    private ProgrammingLanguageSupport languageSupport;
    private Stage primaryStage;
    private boolean isModified = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // 機能管理の初期化
        themeManager = new ThemeManager();
        markdownHighlighter = new MarkdownHighlighter();
        languageSupport = new ProgrammingLanguageSupport();
        
        // UI構築
        BorderPane root = createMainLayout();
        
        // シーンの作成
        Scene scene = new Scene(root, 1200, 800);
        
        // テーマの適用
        themeManager.applyTheme(scene);
        
        // ウィンドウ設定
        primaryStage.setTitle("Markdown対応日本語テキストエディタ");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // フォーカスをテキストエリアに設定
        codeArea.requestFocus();
    }
    
    /**
     * メインレイアウトを作成
     */
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        
        // メニューバーの作成
        MenuBar menuBar = createMenuBar();
        
        // ツールバーの作成
        ToolBar toolBar = createToolBar();
        
        // テキストエリアの作成
        codeArea = createCodeArea();
        
        // 段落折りたたみ機能の初期化
        foldingManager = new ParagraphFoldingManager(codeArea);
        
        // レイアウト配置
        VBox topContainer = new VBox(menuBar, toolBar);
        root.setTop(topContainer);
        root.setCenter(codeArea);
        
        return root;
    }
    
    /**
     * メニューバーを作成
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // ファイルメニュー
        Menu fileMenu = new Menu("ファイル");
        MenuItem newItem = new MenuItem("新規");
        MenuItem openItem = new MenuItem("開く");
        MenuItem saveItem = new MenuItem("保存");
        MenuItem saveAsItem = new MenuItem("名前を付けて保存");
        MenuItem exitItem = new MenuItem("終了");
        
        fileMenu.getItems().addAll(
            newItem, new SeparatorMenuItem(),
            openItem, new SeparatorMenuItem(),
            saveItem, saveAsItem, new SeparatorMenuItem(),
            exitItem
        );
        
        // 編集メニュー
        Menu editMenu = new Menu("編集");
        MenuItem undoItem = new MenuItem("元に戻す");
        MenuItem redoItem = new MenuItem("やり直し");
        MenuItem cutItem = new MenuItem("切り取り");
        MenuItem copyItem = new MenuItem("コピー");
        MenuItem pasteItem = new MenuItem("貼り付け");
        MenuItem selectAllItem = new MenuItem("すべて選択");
        
        editMenu.getItems().addAll(
            undoItem, redoItem, new SeparatorMenuItem(),
            cutItem, copyItem, pasteItem, new SeparatorMenuItem(),
            selectAllItem
        );
        
        // 表示メニュー
        Menu viewMenu = new Menu("表示");
        CheckMenuItem darkModeItem = new CheckMenuItem("ダークモード");
        darkModeItem.setOnAction(e -> themeManager.toggleTheme(primaryStage.getScene()));
        
        viewMenu.getItems().addAll(darkModeItem);
        
        // ヘルプメニュー
        Menu helpMenu = new Menu("ヘルプ");
        MenuItem aboutItem = new MenuItem("バージョン情報");
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);
        
        return menuBar;
    }
    
    /**
     * ツールバーを作成
     */
    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();
        
        Button newButton = new Button("新規");
        Button openButton = new Button("開く");
        Button saveButton = new Button("保存");
        
        Separator separator1 = new Separator();
        
        Button boldButton = new Button("B");
        boldButton.setStyle("-fx-font-weight: bold;");
        Button italicButton = new Button("I");
        italicButton.setStyle("-fx-font-style: italic;");
        Button codeButton = new Button("Code");
        
        Separator separator2 = new Separator();
        
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("Markdown", "Java", "Go", "C/C++", "Haskell", "プレーンテキスト");
        languageCombo.setValue("Markdown");
        
        // 言語切り替えイベント
        languageCombo.setOnAction(e -> {
            String selected = languageCombo.getValue();
            switch (selected) {
                case "Java":
                    languageSupport.setLanguage(ProgrammingLanguageSupport.Language.JAVA);
                    break;
                case "Go":
                    languageSupport.setLanguage(ProgrammingLanguageSupport.Language.GO);
                    break;
                case "C/C++":
                    languageSupport.setLanguage(ProgrammingLanguageSupport.Language.CPP);
                    break;
                case "Haskell":
                    languageSupport.setLanguage(ProgrammingLanguageSupport.Language.HASKELL);
                    break;
                case "Markdown":
                    languageSupport.setLanguage(ProgrammingLanguageSupport.Language.MARKDOWN);
                    break;
                default:
                    languageSupport.setLanguage(ProgrammingLanguageSupport.Language.PLAIN_TEXT);
                    break;
            }
            // ハイライトを更新
            updateSyntaxHighlighting();
        });
        
        toolBar.getItems().addAll(
            newButton, openButton, saveButton, separator1,
            boldButton, italicButton, codeButton, separator2,
            new Label("言語:"), languageCombo
        );
        
        return toolBar;
    }
    
    /**
     * コードエリア（テキストエディタ）を作成
     */
    private CodeArea createCodeArea() {
        CodeArea codeArea = new CodeArea();
        
        // 行番号を表示
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        
        // 日本語フォントの設定
        codeArea.setStyle(
            "-fx-font-family: 'Yu Gothic UI', 'Meiryo', 'MS Gothic', monospace; " +
            "-fx-font-size: 14px;"
        );
        
        // タブサイズの設定
        codeArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB) {
                codeArea.insertText(codeArea.getCaretPosition(), "    ");
                event.consume();
            }
        });
        
        // テキスト変更時のハイライト処理
        codeArea.multiPlainChanges()
            .successionEnds(Duration.ofMillis(500))
            .subscribe(ignore -> updateSyntaxHighlighting());
        
        // 初期テキストの設定
        codeArea.replaceText(0, 0, getInitialText());
        
        return codeArea;
    }
    
    /**
     * 初期表示テキスト
     */
    private String getInitialText() {
        return "# Markdown対応日本語テキストエディタ\n\n" +
               "このエディタは以下の機能に対応しています：\n\n" +
               "## 基本機能\n" +
               "- **日本語完全サポート**\n" +
               "- *Markdown構文ハイライト*\n" +
               "- `コードハイライト`\n" +
               "- 段落の折りたたみ\n" +
               "- ダーク/ライトテーマ切り替え\n\n" +
               "## プログラミング言語サポート\n" +
               "```java\n" +
               "public class HelloWorld {\n" +
               "    public static void main(String[] args) {\n" +
               "        System.out.println(\"こんにちは、世界！\");\n" +
               "    }\n" +
               "}\n" +
               "```\n\n" +
               "### 注意事項\n" +
               "> このエディタはUTF-8エンコーディングに完全対応しています。\n\n" +
               "ファイルの作成と編集を開始してください！\n\n" +
               "### 使用方法\n" +
               "- **段落折りたたみ**: Ctrl + Enter を押しながらマウスクリックで段落を折りたたみ/展開\n" +
               "- **テーマ切り替え**: 表示メニューからダークモードを選択\n" +
               "- **言語切り替え**: ツールバーの言語コンボボックスから選択";
    }
    
    /**
     * シンタックスハイライトを更新
     */
    private void updateSyntaxHighlighting() {
        codeArea.setStyleSpans(0, languageSupport.computeHighlighting(codeArea.getText()));
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}