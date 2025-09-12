package com.texteditor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.fxmisc.richtext.model.StyleSpans;
import org.junit.jupiter.api.Test;

class MarkdownHighlighterTest {

  @Test
  void headingDetected() {
    String md = "# 見出し1\n本文";
    MarkdownHighlighter mh = new MarkdownHighlighter();
    StyleSpans<Collection<String>> spans = mh.computeHighlighting(md);
    boolean hasHeading = false;
    for (int i = 0; i < spans.getSpanCount(); i++) {
      Collection<String> styles = spans.getStyleSpan(i).getStyle();
      if (styles != null && styles.contains("heading1")) {
        hasHeading = true;
        break;
      }
    }
    assertTrue(hasHeading, "heading1 スタイルが検出されるべき");
  }
}
