package com.texteditor;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown構文ハイライト処理クラス
 */
public class MarkdownHighlighter {
    
    // Markdown構文の正規表現パターン
    private static final String[] MARKDOWN_PATTERNS = new String[] {
        "(?<HEADING1>^#{1}\\s+.*)$",        // # 見出し1
        "(?<HEADING2>^#{2}\\s+.*)$",        // ## 見出し2
        "(?<HEADING3>^#{3}\\s+.*)$",        // ### 見出し3
        "(?<HEADING4>^#{4,6}\\s+.*)$",      // #### 見出し4-6
        "(?<BOLD>\\*\\*[^*]+\\*\\*)",       // **太字**
        "(?<ITALIC>\\*[^*]+\\*)",           // *斜体*
        "(?<CODE>`[^`]+`)",                 // `コード`
        "(?<CODEBLOCK>```[\\s\\S]*?```)",   // ```コードブロック```
        "(?<QUOTE>^>.*$)",                  // > 引用
        "(?<LINK>\\[[^\\]]*\\]\\([^\\)]*\\))", // [リンク](URL)
        "(?<IMAGE>!\\[[^\\]]*\\]\\([^\\)]*\\))", // ![画像](URL)
        "(?<LIST>^[\\s]*[\\*\\-\\+]\\s+.*$)", // - リスト
        "(?<NUMLIST>^[\\s]*\\d+\\.\\s+.*$)",  // 1. 番号付きリスト
        "(?<STRIKETHROUGH>~~[^~]+~~)",      // ~~取り消し線~~
        "(?<HORIZONTAL>^[-*_]{3,}$)"        // --- 水平線
    };
    
    private static final Pattern MARKDOWN_PATTERN = Pattern.compile(
        String.join("|", MARKDOWN_PATTERNS),
        Pattern.MULTILINE
    );
    
    /**
     * テキストのハイライト情報を計算
     */
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = MARKDOWN_PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        
        while(matcher.find()) {
            String styleClass = getStyleClass(matcher);
            
            // マッチしない部分はプレーンテキスト
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            
            // マッチした部分にスタイルを適用
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            
            lastKwEnd = matcher.end();
        }
        
        // 残りの部分をプレーンテキストとして追加
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        
        return spansBuilder.create();
    }
    
    /**
     * マッチしたパターンに基づいてスタイルクラスを決定
     */
    private String getStyleClass(Matcher matcher) {
        if (matcher.group("HEADING1") != null) return "heading1";
        if (matcher.group("HEADING2") != null) return "heading2";
        if (matcher.group("HEADING3") != null) return "heading3";
        if (matcher.group("HEADING4") != null) return "heading4";
        if (matcher.group("BOLD") != null) return "bold";
        if (matcher.group("ITALIC") != null) return "italic";
        if (matcher.group("CODE") != null) return "code";
        if (matcher.group("CODEBLOCK") != null) return "codeblock";
        if (matcher.group("QUOTE") != null) return "quote";
        if (matcher.group("LINK") != null) return "link";
        if (matcher.group("IMAGE") != null) return "image";
        if (matcher.group("LIST") != null) return "list";
        if (matcher.group("NUMLIST") != null) return "numlist";
        if (matcher.group("STRIKETHROUGH") != null) return "strikethrough";
        if (matcher.group("HORIZONTAL") != null) return "horizontal";
        
        return "plain";
    }
    
    /**
     * MarkdownハイライトのCSSスタイルを取得
     */
    public String getMarkdownHighlightCSS() {
        return 
            ".heading1 { -fx-font-size: 20px; -fx-font-weight: bold; -fx-fill: #2E74B5; } " +
            ".heading2 { -fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2E74B5; } " +
            ".heading3 { -fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #2E74B5; } " +
            ".heading4 { -fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #2E74B5; } " +
            ".bold { -fx-font-weight: bold; -fx-fill: #323130; } " +
            ".italic { -fx-font-style: italic; -fx-fill: #323130; } " +
            ".code { -fx-background-color: #f6f8fa; -fx-fill: #e36209; -fx-font-family: 'Consolas', 'Courier New', monospace; } " +
            ".codeblock { -fx-background-color: #f6f8fa; -fx-fill: #24292e; -fx-font-family: 'Consolas', 'Courier New', monospace; } " +
            ".quote { -fx-fill: #6a737d; -fx-font-style: italic; } " +
            ".link { -fx-fill: #0366d6; -fx-underline: true; } " +
            ".image { -fx-fill: #28a745; } " +
            ".list { -fx-fill: #323130; } " +
            ".numlist { -fx-fill: #323130; } " +
            ".strikethrough { -fx-strikethrough: true; -fx-fill: #6a737d; } " +
            ".horizontal { -fx-fill: #eaecef; -fx-font-weight: bold; } ";
    }
}