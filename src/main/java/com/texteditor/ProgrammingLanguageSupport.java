package com.texteditor;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/** プログラミング言語サポートクラス Go, C/C++, Haskell, Javaのシンタックスハイライトを提供 */
public class ProgrammingLanguageSupport {

	public enum Language {
		JAVA, GO, CPP, HASKELL, MARKDOWN, PLAIN_TEXT
	}

	// Java構文パターン
	private static final String[] JAVA_PATTERNS = new String[]{
			"(?<JAVAKEYWORD>\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while)\\b)",
			"(?<JAVASTRING>\"([^\"\\\\]|\\\\.)*\")", "(?<JAVACOMMENT>//[^\\r\\n]*|/\\*[\\s\\S]*?\\*/)",
			"(?<JAVANUMBER>\\b\\d+(\\.\\d+)?[fFdDlL]?\\b)"};

	// Go構文パターン
	private static final String[] GO_PATTERNS = new String[]{
			"(?<GOKEYWORD>\\b(break|case|chan|const|continue|default|defer|else|fallthrough|for|func|go|goto|if|import|interface|map|package|range|return|select|struct|switch|type|var)\\b)",
			"(?<GOSTRING>\"([^\"\\\\]|\\\\.)*\"|`[^`]*`)", "(?<GOCOMMENT>//[^\\r\\n]*|/\\*[\\s\\S]*?\\*/)",
			"(?<GONUMBER>\\b\\d+(\\.\\d+)?\\b)"};

	// C/C++構文パターン
	private static final String[] CPP_PATTERNS = new String[]{
			"(?<CPPKEYWORD>\\b(auto|break|case|char|const|continue|default|do|double|else|enum|extern|float|for|goto|if|inline|int|long|register|return|short|signed|sizeof|static|struct|switch|typedef|union|unsigned|void|volatile|while|class|namespace|template|typename|virtual|public|private|protected|using|try|catch|throw|new|delete|this|nullptr)\\b)",
			"(?<CPPSTRING>\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*')",
			"(?<CPPCOMMENT>//[^\\r\\n]*|/\\*[\\s\\S]*?\\*/)", "(?<CPPNUMBER>\\b\\d+(\\.\\d+)?[fFlLuU]*\\b)",
			"(?<CPPPREPROCESSOR>#[^\\r\\n]*)"};

	// Haskell構文パターン
	private static final String[] HASKELL_PATTERNS = new String[]{
			"(?<HASKELLKEYWORD>\\b(case|class|data|default|deriving|do|else|foreign|if|import|in|infix|infixl|infixr|instance|let|module|newtype|of|then|type|where|as|qualified|hiding)\\b)",
			"(?<HASKELLSTRING>\"([^\"\\\\]|\\\\.)*\")", "(?<HASKELLCOMMENT>--[^\\r\\n]*|\\{-[\\s\\S]*?-\\})",
			"(?<HASKELLNUMBER>\\b\\d+(\\.\\d+)?\\b)", "(?<HASKELLOPERATOR>[=><+\\-*/&|!@#$%^&*()\\[\\]{}.,;:])"};

	private Language currentLanguage = Language.MARKDOWN;

	/** 現在の言語を設定 */
	public void setLanguage(Language language) {
		this.currentLanguage = language;
	}

	/** 現在の言語を取得 */
	public Language getCurrentLanguage() {
		return currentLanguage;
	}

	/** テキストのシンタックスハイライトを計算 */
	public StyleSpans<Collection<String>> computeHighlighting(String text) {
		switch (currentLanguage) {
			case JAVA :
				return computeJavaHighlighting(text);
			case GO :
				return computeGoHighlighting(text);
			case CPP :
				return computeCppHighlighting(text);
			case HASKELL :
				return computeHaskellHighlighting(text);
			case MARKDOWN :
				return new MarkdownHighlighter().computeHighlighting(text);
			default :
				return computePlainTextHighlighting(text);
		}
	}

	/** Javaシンタックスハイライト */
	private StyleSpans<Collection<String>> computeJavaHighlighting(String text) {
		return computeHighlightingForPatterns(text, JAVA_PATTERNS, "JAVA");
	}

	/** Goシンタックスハイライト */
	private StyleSpans<Collection<String>> computeGoHighlighting(String text) {
		return computeHighlightingForPatterns(text, GO_PATTERNS, "GO");
	}

	/** C/C++シンタックスハイライト */
	private StyleSpans<Collection<String>> computeCppHighlighting(String text) {
		return computeHighlightingForPatterns(text, CPP_PATTERNS, "CPP");
	}

	/** Haskellシンタックスハイライト */
	private StyleSpans<Collection<String>> computeHaskellHighlighting(String text) {
		return computeHighlightingForPatterns(text, HASKELL_PATTERNS, "HASKELL");
	}

	/** プレーンテキストハイライト（何もしない） */
	private StyleSpans<Collection<String>> computePlainTextHighlighting(String text) {
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		spansBuilder.add(Collections.emptyList(), text.length());
		return spansBuilder.create();
	}

	/** パターンに基づくハイライト計算 */
	private StyleSpans<Collection<String>> computeHighlightingForPatterns(String text, String[] patterns,
			String prefix) {
		Pattern pattern = Pattern.compile(String.join("|", patterns));
		Matcher matcher = pattern.matcher(text);
		int lastKwEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

		while (matcher.find()) {
			String styleClass = getStyleClassForMatch(matcher, prefix);

			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}

		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
		return spansBuilder.create();
	}

	/** マッチした内容に基づいてスタイルクラスを決定 */
	private String getStyleClassForMatch(Matcher matcher, String prefix) {
		if (matcher.group(prefix + "KEYWORD") != null)
			return "keyword";
		if (matcher.group(prefix + "STRING") != null)
			return "string";
		if (matcher.group(prefix + "COMMENT") != null)
			return "comment";
		if (matcher.group(prefix + "NUMBER") != null)
			return "number";
		if (prefix.equals("CPP") && matcher.group("CPPPREPROCESSOR") != null)
			return "preprocessor";
		if (prefix.equals("HASKELL") && matcher.group("HASKELLOPERATOR") != null)
			return "operator";
		return "plain";
	}

	/** プログラミング言語ハイライト用CSSを取得 */
	public String getProgrammingLanguageCSS() {
		return ".keyword { -fx-fill: #0000ff; -fx-font-weight: bold; } " + ".string { -fx-fill: #008000; } "
				+ ".comment { -fx-fill: #808080; -fx-font-style: italic; } " + ".number { -fx-fill: #ff8000; } "
				+ ".preprocessor { -fx-fill: #800080; } " + ".operator { -fx-fill: #ff0000; } " + "/* ダークテーマ用 */ "
				+ ".root.dark-theme .keyword { -fx-fill: #569cd6; } "
				+ ".root.dark-theme .string { -fx-fill: #ce9178; } "
				+ ".root.dark-theme .comment { -fx-fill: #6a9955; } "
				+ ".root.dark-theme .number { -fx-fill: #b5cea8; } "
				+ ".root.dark-theme .preprocessor { -fx-fill: #c586c0; } "
				+ ".root.dark-theme .operator { -fx-fill: #dcdcaa; } ";
	}
}
