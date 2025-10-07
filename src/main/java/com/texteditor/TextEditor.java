package com.texteditor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/** Markdown対応日本語テキストエディタのメインクラス */
public class TextEditor extends Application {

	private CodeArea codeArea;
	private ThemeManager themeManager;
	private ProgrammingLanguageSupport languageSupport;
	private Stage primaryStage;
	private boolean isModified = false;
	private Path currentFile = null;
	private static final int INDENT_SIZE = 4;
	private static final long HIGHLIGHT_DEBOUNCE_MS = 500;
	// プレビュー関連
	private javafx.scene.control.SplitPane editorSplit;
	private javafx.scene.web.WebView previewView;
	private boolean previewEnabled = false;
	private com.vladsch.flexmark.parser.Parser mdParser;
	private com.vladsch.flexmark.html.HtmlRenderer mdRenderer;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;

		// 機能管理の初期化
		themeManager = new ThemeManager();
		languageSupport = new ProgrammingLanguageSupport();

		// UI構築
		BorderPane root = createMainLayout();

		// シーンの作成
		Scene scene = new Scene(root, 1200, 800);

		// スタイルシートを追加（Markdown ハイライト + プログラミング言語 + ベースUI）
		try {
			String mdCss = getClass().getResource("/markdown-highlight.css").toExternalForm();
			scene.getStylesheets().add(mdCss);
		} catch (Exception ex) {
			// リソースが見つからない場合はスキップ（ログ等に拡張可）
		}
		try {
			String progCss = getClass().getResource("/programming-highlight.css").toExternalForm();
			scene.getStylesheets().add(progCss);
		} catch (Exception ex) {
			// リソースが見つからない場合はスキップ
		}
		try {
			String appCss = getClass().getResource("/app.css").toExternalForm();
			scene.getStylesheets().add(appCss);
		} catch (Exception ex) {
			// リソースが見つからない場合はスキップ
		}

		// テーマの適用
		themeManager.applyTheme(scene);

		// ウィンドウ設定
		primaryStage.setTitle("Markdown対応日本語テキストエディタ");
		primaryStage.setScene(scene);
		primaryStage.show();

		// フォーカスをテキストエリアに設定
		codeArea.requestFocus();
	}

	/** メインレイアウトを作成 */
	private BorderPane createMainLayout() {
		BorderPane root = new BorderPane();

		// メニューバーの作成
		MenuBar menuBar = createMenuBar();

		// ツールバーの作成
		ToolBar toolBar = createToolBar();

		// テキストエリアの作成
		codeArea = createCodeArea();

		// 折りたたみ機能の設定
		new ParagraphFoldingManager(codeArea);

		// Markdown パーサ/レンダラ
		mdParser = com.vladsch.flexmark.parser.Parser.builder().build();
		mdRenderer = com.vladsch.flexmark.html.HtmlRenderer.builder().build();

		// プレビューUI
		previewView = new javafx.scene.web.WebView();
		previewView.setContextMenuEnabled(false);
		editorSplit = new javafx.scene.control.SplitPane();
		editorSplit.setDividerPositions(0.5);

		// レイアウト配置
		VBox topContainer = new VBox(menuBar, toolBar);
		root.setTop(topContainer);
		refreshCenterContent(root);

		// 初期のハイライト/プレビューを適用
		updateSyntaxHighlighting();
		updatePreview();

		return root;
	}

	/** メニューバーを作成 */
	private MenuBar createMenuBar() {
		MenuBar menuBar = new MenuBar();

		// ファイルメニュー
		Menu fileMenu = new Menu("ファイル");
		MenuItem newItem = new MenuItem("新規");
		MenuItem openItem = new MenuItem("開く");
		MenuItem saveItem = new MenuItem("保存");
		MenuItem saveAsItem = new MenuItem("名前を付けて保存");
		MenuItem exitItem = new MenuItem("終了");

		// ショートカット設定（プラットフォーム依存の修飾キーに追従: Windows/Linux=Ctrl, macOS=Command）
		newItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
		openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
		saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
		exitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));

		// アクション設定
		newItem.setOnAction(e -> doNew());
		openItem.setOnAction(e -> doOpen());
		saveItem.setOnAction(e -> doSave());
		saveAsItem.setOnAction(e -> doSaveAs());
		exitItem.setOnAction(e -> doExit());

		fileMenu.getItems().addAll(newItem, new SeparatorMenuItem(), openItem, new SeparatorMenuItem(), saveItem,
				saveAsItem, new SeparatorMenuItem(), exitItem);

		// 編集メニュー
		Menu editMenu = new Menu("編集");
		MenuItem undoItem = new MenuItem("元に戻す");
		MenuItem redoItem = new MenuItem("やり直し");
		MenuItem cutItem = new MenuItem("切り取り");
		MenuItem copyItem = new MenuItem("コピー");
		MenuItem pasteItem = new MenuItem("貼り付け");
		MenuItem selectAllItem = new MenuItem("すべて選択");

		// 編集メニューのアクション
		undoItem.setOnAction(e -> codeArea.undo());
		redoItem.setOnAction(e -> codeArea.redo());
		cutItem.setOnAction(e -> codeArea.cut());
		copyItem.setOnAction(e -> codeArea.copy());
		pasteItem.setOnAction(e -> codeArea.paste());
		selectAllItem.setOnAction(e -> codeArea.selectAll());

		editMenu.getItems().addAll(undoItem, redoItem, new SeparatorMenuItem(), cutItem, copyItem, pasteItem,
				new SeparatorMenuItem(), selectAllItem);

		// 表示メニュー
		Menu viewMenu = new Menu("表示");
		CheckMenuItem darkModeItem = new CheckMenuItem("ダークモード");
		// 初期状態を同期
		darkModeItem.setSelected(themeManager.isDarkMode());
		darkModeItem.setOnAction(e -> {
			themeManager.toggleTheme(primaryStage.getScene());
			// トグル後の状態を同期
			darkModeItem.setSelected(themeManager.isDarkMode());
		});

		CheckMenuItem previewItem = new CheckMenuItem("Markdownプレビュー");
		previewItem.setSelected(previewEnabled);
		previewItem.setOnAction(e -> {
			previewEnabled = previewItem.isSelected();
			refreshCenterContent((BorderPane) primaryStage.getScene().getRoot());
			updatePreview();
		});

		viewMenu.getItems().addAll(darkModeItem, new SeparatorMenuItem(), previewItem);

		// ヘルプメニュー
		Menu helpMenu = new Menu("ヘルプ");
		MenuItem aboutItem = new MenuItem("バージョン情報");
		helpMenu.getItems().add(aboutItem);

		menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);

		return menuBar;
	}

	/** ツールバーを作成 */
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
				case "Java" :
					languageSupport.setLanguage(ProgrammingLanguageSupport.Language.JAVA);
					break;
				case "Go" :
					languageSupport.setLanguage(ProgrammingLanguageSupport.Language.GO);
					break;
				case "C/C++" :
					languageSupport.setLanguage(ProgrammingLanguageSupport.Language.CPP);
					break;
				case "Haskell" :
					languageSupport.setLanguage(ProgrammingLanguageSupport.Language.HASKELL);
					break;
				case "Markdown" :
					languageSupport.setLanguage(ProgrammingLanguageSupport.Language.MARKDOWN);
					break;
				default :
					languageSupport.setLanguage(ProgrammingLanguageSupport.Language.PLAIN_TEXT);
					break;
			}
			// ハイライトを更新
			updateSyntaxHighlighting();
		});

		// ツールバーのアクション
		newButton.setOnAction(e -> doNew());
		openButton.setOnAction(e -> doOpen());
		saveButton.setOnAction(e -> doSave());
		boldButton.setOnAction(e -> applyInlineWrap("**", "**"));
		italicButton.setOnAction(e -> applyInlineWrap("*", "*"));
		codeButton.setOnAction(e -> applyInlineWrap("`", "`"));

		toolBar.getItems().addAll(newButton, openButton, saveButton, separator1, boldButton, italicButton, codeButton,
				separator2, new Label("言語:"), languageCombo);

		return toolBar;
	}

	/** コードエリア（テキストエディタ）を作成 */
	private CodeArea createCodeArea() {
		CodeArea codeArea = new CodeArea();

		// 行番号を表示
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

		// 日本語フォントの設定
		codeArea.setStyle(
				"-fx-font-family: 'Yu Gothic UI', 'Meiryo', 'MS Gothic', monospace; " + "-fx-font-size: 14px;");

		// タブサイズの設定
		codeArea.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.TAB) {
				if (event.isShiftDown()) {
					unindentSelectionOrCaret();
					event.consume();
				} else if (codeArea.getSelection().getLength() > 0) {
					indentSelection();
					event.consume();
				} else {
					codeArea.insertText(codeArea.getCaretPosition(), " ".repeat(INDENT_SIZE));
					event.consume();
				}
			}
		});

		// テキスト変更時のハイライト処理
		codeArea.multiPlainChanges().successionEnds(Duration.ofMillis(HIGHLIGHT_DEBOUNCE_MS)).subscribe(ignore -> {
			updateSyntaxHighlighting();
			updatePreview();
			markModified(true);
		});

		// 初期テキストの設定
		codeArea.replaceText(0, 0, getInitialText());

		return codeArea;
	}

	/** 初期表示テキスト */
	private String getInitialText() {
		return getDefaultInitialText();
	}

	private String getDefaultInitialText() {
		return "# Markdown対応日本語テキストエディタ\n\n" + "このエディタは以下の機能に対応しています：\n\n" + "## 基本機能\n" + "- **日本語完全サポート**\n"
				+ "- *Markdown構文ハイライト*\n" + "- `コードハイライト`\n" + "- 段落の折りたたみ\n" + "- ダーク/ライトテーマ切り替え\n\n"
				+ "## プログラミング言語サポート\n" + "```java\n" + "public class HelloWorld {\n"
				+ "    public static void main(String[] args) {\n" + "        System.out.println(\"こんにちは、世界！\");\n"
				+ "    }\n" + "}\n" + "```\n\n" + "### 注意事項\n" + "> このエディタはUTF-8エンコーディングに完全対応しています。\n\n"
				+ "ファイルの作成と編集を開始してください！\n\n" + "### 使用方法\n" + "- **段落折りたたみ**: Ctrl + Enter を押しながらマウスクリックで段落を折りたたみ/展開\n"
				+ "- **テーマ切り替え**: 表示メニューからダークモードを選択\n" + "- **言語切り替え**: ツールバーの言語コンボボックスから選択";
	}

	/** シンタックスハイライトを更新 */
	// ===== ファイル操作関連 =====
	private void doNew() {
		if (!confirmSaveIfModified())
			return;
		codeArea.clear();
		codeArea.replaceText(0, 0, getDefaultInitialText());
		currentFile = null;
		markModified(false);
	}

	private void doOpen() {
		if (!confirmSaveIfModified())
			return;
		FileChooser chooser = new FileChooser();
		chooser.setTitle("開く");
		chooser.getExtensionFilters()
				.addAll(new FileChooser.ExtensionFilter("テキスト/Markdown", "*.txt", "*.md", "*.markdown", "*.*"));
		Path last = currentFile;
		if (last != null && last.getParent() != null) {
			chooser.setInitialDirectory(last.getParent().toFile());
		}
		var file = chooser.showOpenDialog(primaryStage);
		if (file != null) {
			try {
				Path filePath = file.toPath();
				if (!Files.exists(filePath)) {
					showError("エラー", "指定されたファイルは存在しません: " + filePath);
					return;
				}
				if (!Files.isReadable(filePath)) {
					showError("エラー", "ファイルを読み取る権限がありません: " + filePath);
					return;
				}
				String content = Files.readString(filePath, StandardCharsets.UTF_8);
				codeArea.replaceText(content);
				currentFile = filePath;
				markModified(false);
				updateSyntaxHighlighting();
			} catch (IOException ex) {
				showError("ファイルを開けませんでした", "ファイルの読み込み中にエラーが発生しました: " + ex.getMessage());
			} catch (SecurityException ex) {
				showError("セキュリティエラー", "ファイルにアクセスする権限がありません: " + ex.getMessage());
			}
		}
	}

	private void doSave() {
		if (currentFile == null) {
			doSaveAs();
		} else {
			try {
				Files.writeString(currentFile, codeArea.getText(), StandardCharsets.UTF_8);
				markModified(false);
			} catch (IOException ex) {
				showError("保存に失敗しました", ex.getMessage());
			}
		}
	}

	private void doSaveAs() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("名前を付けて保存");
		chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Markdown (*.md)", "*.md"),
				new FileChooser.ExtensionFilter("テキスト (*.txt)", "*.txt"),
				new FileChooser.ExtensionFilter("すべてのファイル (*.*)", "*.*"));
		if (currentFile != null) {
			chooser.setInitialDirectory(currentFile.getParent().toFile());
			chooser.setInitialFileName(currentFile.getFileName().toString());
		}
		var file = chooser.showSaveDialog(primaryStage);
		if (file != null) {
			try {
				Files.writeString(file.toPath(), codeArea.getText(), StandardCharsets.UTF_8);
				currentFile = file.toPath();
				markModified(false);
			} catch (IOException ex) {
				showError("保存に失敗しました", ex.getMessage());
			}
		}
	}

	private void doExit() {
		if (!confirmSaveIfModified())
			return;
		primaryStage.close();
	}

	private boolean confirmSaveIfModified() {
		if (!isModified)
			return true;
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("未保存の変更");
		alert.setHeaderText("変更内容を保存しますか？");
		alert.setContentText("保存しない場合、変更は失われます。");
		ButtonType save = new ButtonType("保存");
		ButtonType dont = new ButtonType("保存しない");
		ButtonType cancel = new ButtonType("キャンセル", ButtonBar.ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(save, dont, cancel);
		var result = alert.showAndWait();
		if (result.isEmpty() || result.get() == cancel)
			return false;
		if (result.get() == save) {
			doSave();
			return !isModified; // 保存成功で false になる
		}
		return true;
	}

	private void showError(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void updateSyntaxHighlighting() {
		codeArea.setStyleSpans(0, languageSupport.computeHighlighting(codeArea.getText()));
	}

	/** Markdownプレビュー更新（有効時のみ） */
	private void updatePreview() {
		if (!previewEnabled || previewView == null || mdParser == null || mdRenderer == null)
			return;
		String md = codeArea.getText();
		String html = mdRenderer.render(mdParser.parse(md));
		String styledHtml = "<html><head><meta charset='UTF-8'>"
				+ "<style>body{font:14px -apple-system,Segoe UI,Roboto,\"Noto Sans JP\",sans-serif;padding:12px;}"
				+ "pre,code{font-family:Consolas,\"Courier New\",monospace;}</style>" + "</head><body>" + html
				+ "</body></html>";
		previewView.getEngine().loadContent(styledHtml);
	}

	/** センター領域の切替（プレビュー有効/無効） */
	private void refreshCenterContent(BorderPane root) {
		if (previewEnabled) {
			editorSplit.getItems().setAll(codeArea, previewView);
			root.setCenter(editorSplit);
		} else {
			root.setCenter(codeArea);
		}
	}

	private void markModified(boolean modified) {
		this.isModified = modified;
		String baseTitle = "Markdown対応日本語テキストエディタ";
		String filePart = (currentFile != null ? " - " + currentFile.getFileName().toString() : "");
		String modifiedMark = isModified ? "*" : "";
		if (primaryStage != null) {
			primaryStage.setTitle(modifiedMark + baseTitle + filePart);
		}
	}

	private void indentSelection() {
		int start = codeArea.getSelection().getStart();
		int end = codeArea.getSelection().getEnd();
		int startPar = codeArea.offsetToPosition(start, org.fxmisc.richtext.model.TwoDimensional.Bias.Forward)
				.getMajor();
		int endPar = codeArea.offsetToPosition(end, org.fxmisc.richtext.model.TwoDimensional.Bias.Backward).getMajor();
		for (int i = startPar; i <= endPar; i++) {
			int parStart = codeArea.getAbsolutePosition(i, 0);
			codeArea.insertText(parStart, " ".repeat(INDENT_SIZE));
		}
	}

	private void unindentSelectionOrCaret() {
		if (codeArea.getSelection().getLength() > 0) {
			int start = codeArea.getSelection().getStart();
			int end = codeArea.getSelection().getEnd();
			int startPar = codeArea.offsetToPosition(start, org.fxmisc.richtext.model.TwoDimensional.Bias.Forward)
					.getMajor();
			int endPar = codeArea.offsetToPosition(end, org.fxmisc.richtext.model.TwoDimensional.Bias.Backward)
					.getMajor();
			for (int i = startPar; i <= endPar; i++) {
				int parStart = codeArea.getAbsolutePosition(i, 0);
				int remove = Math.min(INDENT_SIZE, countLeadingSpacesFrom(parStart));
				if (remove > 0) {
					codeArea.deleteText(parStart, parStart + remove);
				}
			}
		} else {
			int caret = codeArea.getCaretPosition();
			int lineStart = codeArea.getAbsolutePosition(codeArea.getCurrentParagraph(), 0);
			int leading = Math.min(INDENT_SIZE, countLeadingSpacesBetween(lineStart, caret));
			if (leading > 0) {
				codeArea.deleteText(caret - leading, caret);
			}
		}
	}

	/** 選択範囲を囲む（未選択時はトークンを挿入してキャレットを内側へ） */
	private void applyInlineWrap(String open, String close) {
		int start = codeArea.getSelection().getStart();
		int end = codeArea.getSelection().getEnd();
		if (end > start) {
			String selected = codeArea.getSelectedText();
			codeArea.replaceSelection(open + selected + close);
			codeArea.selectRange(start + open.length(), start + open.length() + selected.length());
		} else {
			int caret = codeArea.getCaretPosition();
			codeArea.insertText(caret, open + close);
			codeArea.displaceCaret(caret + open.length());
		}
		updateSyntaxHighlighting();
		updatePreview();
		markModified(true);
	}

	private int countLeadingSpacesFrom(int pos) {
		int paragraph = codeArea.offsetToPosition(pos, org.fxmisc.richtext.model.TwoDimensional.Bias.Forward)
				.getMajor();
		int parStart = codeArea.getAbsolutePosition(paragraph, 0);
		int parEnd = parStart + codeArea.getParagraph(paragraph).length();
		// pos は段落先頭想定だが、安全のため parStart..parEnd にクランプ
		int start = Math.max(pos, parStart);
		int end = parEnd;
		return countLeadingSpacesBetween(start, end);
	}

	private int countLeadingSpacesBetween(int start, int end) {
		int count = 0;
		int limit = Math.min(start + INDENT_SIZE, end);
		for (int i = start; i < limit; i++) {
			char c = codeArea.getText(i, i + 1).charAt(0);
			if (c == ' ')
				count++;
			else
				break;
		}
		return count;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
