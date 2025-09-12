package com.texteditor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fxmisc.richtext.CodeArea;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/** 段落折りたたみ機能を管理するクラス Ctrl + Enter + マウスクリックで段落の展開/折りたたみを制御 */
public class ParagraphFoldingManager {

  private final CodeArea codeArea;
  private final Set<Integer> foldedParagraphs = new HashSet<>();
  private final Map<Integer, String> foldedContent = new HashMap<>();
  private boolean enterPressed = false;
  private static final String FOLD_MARKER = " [...]";

  public ParagraphFoldingManager(CodeArea codeArea) {
    this.codeArea = codeArea;
    setupEventHandlers();
  }

  /** イベントハンドラーを設定 */
  private void setupEventHandlers() {
    // 既存ハンドラと競合しないよう addEventHandler を使用
    codeArea.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    codeArea.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
    codeArea.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
  }

  /** キー押下イベントの処理 */
  private void handleKeyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER && (event.isControlDown() || event.isMetaDown())) {
      // 修飾キーが押下されている状態で Enter が押されたときのみ有効化
      enterPressed = true;
      event.consume(); // デフォルトの改行動作を防ぐ
    }
  }

  /** キー離上イベントの処理 */
  private void handleKeyReleased(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER) {
      // Enter キーが離されたらフラグを解除
      enterPressed = false;
    }
  }

  /** マウスクリックイベントの処理 */
  private void handleMouseClicked(MouseEvent event) {
    boolean modifierDown = event.isControlDown() || event.isMetaDown();
    if (modifierDown && enterPressed) {
      int paragraph = codeArea.getCurrentParagraph();

      if (isFolded(paragraph)) {
        expandParagraph(paragraph);
      } else {
        foldParagraph(paragraph);
      }

      event.consume();
      // 一度処理したら誤作動を防ぐため解除
      enterPressed = false;
    }
  }

  /** 段落を折りたたむ */
  private void foldParagraph(int paragraphIndex) {
    try {
      if (paragraphIndex < 0 || paragraphIndex >= codeArea.getParagraphs().size()) {
        return;
      }

      ParagraphRange range = getParagraphRange(paragraphIndex);
      if (range.endParagraph <= range.startParagraph) {
        return; // 折りたたみ対象がない
      }

      // 折りたたみ対象のテキストを保存
      StringBuilder foldedText = new StringBuilder();
      for (int i = range.startParagraph + 1; i <= range.endParagraph; i++) {
        if (i < codeArea.getParagraphs().size()) {
          if (i > range.startParagraph + 1) {
            foldedText.append("\n");
          }
          foldedText.append(codeArea.getParagraph(i).getText());
        }
      }

      foldedContent.put(paragraphIndex, foldedText.toString());
      foldedParagraphs.add(paragraphIndex);

      // テキストを置換（折りたたみ表示）
      String headerText = codeArea.getParagraph(range.startParagraph).getText();
      if (headerText.isEmpty()) {
        return; // ヘッダーが空の場合は折りたたみ不可
      }

      // 折りたたみ範囲のテキストを削除
      int startPos = codeArea.getAbsolutePosition(range.startParagraph + 1, 0);
      int endPos =
          range.endParagraph < codeArea.getParagraphs().size()
              ? codeArea.getAbsolutePosition(range.endParagraph + 1, 0)
              : codeArea.getLength();

      if (startPos < endPos) {
        codeArea.deleteText(startPos, endPos);
      }

      // 折りたたみマーカーを追加
      int headerEndPos = codeArea.getAbsolutePosition(range.startParagraph, headerText.length());
      codeArea.insertText(headerEndPos, FOLD_MARKER);

      updateFoldingIndicators();
    } catch (Exception e) {
      System.err.println("Error folding paragraph: " + e.getMessage());
    }
  }

  /** 段落を展開する */
  private void expandParagraph(int paragraphIndex) {
    try {
      if (!foldedParagraphs.contains(paragraphIndex)) {
        return;
      }

      String content = foldedContent.get(paragraphIndex);
      if (content == null) {
        return;
      }

      // 折りたたみマーカーを削除
      String currentText = codeArea.getParagraph(paragraphIndex).getText();
      if (currentText.endsWith(FOLD_MARKER)) {
        int markerStart = currentText.length() - FOLD_MARKER.length();
        int absoluteMarkerStart = codeArea.getAbsolutePosition(paragraphIndex, markerStart);
        int absoluteMarkerEnd = codeArea.getAbsolutePosition(paragraphIndex, currentText.length());
        codeArea.deleteText(absoluteMarkerStart, absoluteMarkerEnd);
      }

      // 元のコンテンツを挿入
      int insertPos =
          codeArea.getAbsolutePosition(
              paragraphIndex, codeArea.getParagraph(paragraphIndex).getText().length());
      if (!content.isEmpty()) {
        codeArea.insertText(insertPos, "\n" + content);
      }

      // 折りたたみ状態をクリア
      foldedParagraphs.remove(paragraphIndex);
      foldedContent.remove(paragraphIndex);

      updateFoldingIndicators();
    } catch (Exception e) {
      System.err.println("Error expanding paragraph: " + e.getMessage());
    }
  }

  /** 段落の範囲を取得 */
  private ParagraphRange getParagraphRange(int startParagraph) {
    ParagraphRange range = new ParagraphRange();
    range.startParagraph = startParagraph;
    range.endParagraph = startParagraph;

    String startText = codeArea.getParagraph(startParagraph).getText().trim();

    // 見出しの場合、次の同レベル以上の見出しまで
    if (startText.startsWith("#")) {
      int headerLevel = getHeaderLevel(startText);
      for (int i = startParagraph + 1; i < codeArea.getParagraphs().size(); i++) {
        String text = codeArea.getParagraph(i).getText().trim();
        if (text.startsWith("#") && getHeaderLevel(text) <= headerLevel) {
          break;
        }
        range.endParagraph = i;
      }
    }
    // リストの場合、連続するリスト項目
    else if (isListItem(startText)) {
      for (int i = startParagraph + 1; i < codeArea.getParagraphs().size(); i++) {
        String text = codeArea.getParagraph(i).getText().trim();
        if (!isListItem(text) && !text.isEmpty()) {
          break;
        }
        if (!text.isEmpty()) {
          range.endParagraph = i;
        }
      }
    }
    // その他の場合、次の空行または構造的要素まで
    else {
      for (int i = startParagraph + 1; i < codeArea.getParagraphs().size(); i++) {
        String text = codeArea.getParagraph(i).getText().trim();
        if (text.isEmpty() || text.startsWith("#") || isListItem(text) || text.startsWith(">")) {
          break;
        }
        range.endParagraph = i;
      }
      // 範囲が最小1行以上であることを保証
      if (range.endParagraph == range.startParagraph) {
        range.endParagraph =
            Math.min(range.startParagraph + 1, codeArea.getParagraphs().size() - 1);
      }
    }

    return range;
  }

  /** 見出しレベルを取得 */
  private int getHeaderLevel(String text) {
    int level = 0;
    for (char c : text.toCharArray()) {
      if (c == '#') {
        level++;
      } else {
        break;
      }
    }
    return level;
  }

  /** リスト項目かどうかを判定 */
  private boolean isListItem(String text) {
    return text.matches("^\\s*[-*+]\\s+.*") || text.matches("^\\s*\\d+\\.\\s+.*");
  }

  /** 段落が折りたたまれているかを確認 */
  private boolean isFolded(int paragraphIndex) {
    return foldedParagraphs.contains(paragraphIndex);
  }

  /** 折りたたみインジケーターを更新 */
  private void updateFoldingIndicators() {
    // 行番号の横に折りたたみインジケーターを表示
    // 実装例: 行番号のカスタム描画で折りたたみ状態を表示
    // 現在は簡易実装としてログ出力
    // System.out.println("Folded paragraphs: " + foldedParagraphs);
  }

  /** 段落範囲を表すクラス */
  private static class ParagraphRange {
    int startParagraph;
    int endParagraph;
  }

  /** 折りたたみ状態をクリア */
  public void clearAllFolding() {
    for (int paragraph : new HashSet<>(foldedParagraphs)) {
      expandParagraph(paragraph);
    }
  }

  /** 折りたたまれた段落のリストを取得 */
  public Set<Integer> getFoldedParagraphs() {
    return new HashSet<>(foldedParagraphs);
  }
}
