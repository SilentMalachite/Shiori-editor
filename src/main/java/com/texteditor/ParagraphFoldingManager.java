package com.texteditor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.fxmisc.richtext.CodeArea;

/**
 * Manages paragraph folding features. Controls paragraph expansion/collapse
 * with Ctrl + Enter + mouse click
 */
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

	/** Sets up event handlers */
	private void setupEventHandlers() {
		// Existing handlers are not conflicted with addEventHandler
		codeArea.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
		codeArea.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
		codeArea.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
	}

	/** Handles key press events */
	private void handleKeyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER && (event.isControlDown() || event.isMetaDown())) {
			// Activates only when Enter is pressed with modifier key
			enterPressed = true;
			event.consume(); // Prevents default line break behavior
		}
	}

	/** Handles key release events */
	private void handleKeyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			// Resets flag when Enter is released
			enterPressed = false;
		}
	}

	/** Handles mouse click events */
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
			// Resets to prevent accidental triggering
			enterPressed = false;
		}
	}

	/** Folds the paragraph */
	private void foldParagraph(int paragraphIndex) {
		try {
			if (paragraphIndex < 0 || paragraphIndex >= codeArea.getParagraphs().size()) {
				return;
			}

			if (isFolded(paragraphIndex)) {
				return; // Skips if already folded
			}

			ParagraphRange range = getParagraphRange(paragraphIndex);
			if (range.endParagraph <= range.startParagraph) {
				return; // No content to fold
			}

			// Saves content to fold
			StringBuilder foldedText = new StringBuilder();
			for (int i = range.startParagraph + 1; i <= range.endParagraph; i++) {
				if (i < codeArea.getParagraphs().size()) {
					String paraText = codeArea.getParagraph(i).getText();
					if (paraText != null) {
						if (i > range.startParagraph + 1) {
							foldedText.append("\n");
						}
						foldedText.append(paraText);
					}
				}
			}

			foldedContent.put(paragraphIndex, foldedText.toString());
			foldedParagraphs.add(paragraphIndex);

			// Replaces text with folding display
			String headerText = codeArea.getParagraph(range.startParagraph).getText();
			if (headerText == null || headerText.isEmpty()) {
				return; // Cannot fold if header is empty or null
			}

			// Deletes content in folding range
			int startPos = codeArea.getAbsolutePosition(range.startParagraph + 1, 0);
			int endPos = range.endParagraph < codeArea.getParagraphs().size()
					? codeArea.getAbsolutePosition(range.endParagraph + 1, 0)
					: codeArea.getLength();

			if (startPos < endPos) {
				codeArea.deleteText(startPos, endPos);
			}

			// Adds folding marker
			int headerEndPos = codeArea.getAbsolutePosition(range.startParagraph, headerText.length());
			codeArea.insertText(headerEndPos, FOLD_MARKER);

			updateFoldingIndicators();
		} catch (Exception e) {
			System.err.println("Error folding paragraph at " + paragraphIndex + ": " + e.getMessage());
		}
	}

	/** Expands the paragraph */
	private void expandParagraph(int paragraphIndex) {
		try {
			if (!foldedParagraphs.contains(paragraphIndex)) {
				return;
			}

			String content = foldedContent.get(paragraphIndex);
			if (content == null) {
				return;
			}

			// Removes folding marker
			String currentText = codeArea.getParagraph(paragraphIndex).getText();
			if (currentText == null) {
				return; // Cannot process if text is null
			}
			int markerIndex = currentText.lastIndexOf(FOLD_MARKER);
			if (markerIndex != -1) {
				int markerStart = markerIndex;
				int markerEnd = markerIndex + FOLD_MARKER.length();
				int absoluteMarkerStart = codeArea.getAbsolutePosition(paragraphIndex, markerStart);
				int absoluteMarkerEnd = codeArea.getAbsolutePosition(paragraphIndex, markerEnd);
				codeArea.deleteText(absoluteMarkerStart, absoluteMarkerEnd);
			}

			// Inserts original content
			int insertPos = codeArea.getAbsolutePosition(paragraphIndex,
					codeArea.getParagraph(paragraphIndex).getText().length());
			if (!content.isEmpty()) {
				codeArea.insertText(insertPos, "\n" + content);
			}

			// Clears folding state
			foldedParagraphs.remove(paragraphIndex);
			foldedContent.remove(paragraphIndex);

			updateFoldingIndicators();
		} catch (Exception e) {
			System.err.println("Error expanding paragraph at " + paragraphIndex + ": " + e.getMessage());
		}
	}

	/** Gets the range of the paragraph */
	private ParagraphRange getParagraphRange(int startParagraph) {
		ParagraphRange range = new ParagraphRange();
		range.startParagraph = startParagraph;
		range.endParagraph = startParagraph;

		var paragraphs = codeArea.getParagraphs();
		int size = paragraphs.size();

		String startText = paragraphs.get(startParagraph).getText();
		if (startText == null) {
			startText = "";
		}
		startText = startText.trim();

		// Goes up to the next higher level header
		if (startText.startsWith("#")) {
			int headerLevel = getHeaderLevel(startText);
			for (int i = startParagraph + 1; i < size; i++) {
				var para = paragraphs.get(i);
				String text = para.getText();
				if (text == null) {
					text = "";
				}
				text = text.trim();
				if (text.startsWith("#") && getHeaderLevel(text) < headerLevel) {
					break;
				}
				range.endParagraph = i;
			}
		}
		// For lists, continuous list items
		else if (isListItem(startText)) {
			for (int i = startParagraph + 1; i < size; i++) {
				var para = paragraphs.get(i);
				String text = para.getText();
				if (text == null) {
					text = "";
				}
				text = text.trim();
				if (!isListItem(text) && !text.isEmpty()) {
					break;
				}
				if (!text.isEmpty()) {
					range.endParagraph = i;
				}
			}
		}
		// For others, up to the next empty line or structural element
		else {
			for (int i = startParagraph + 1; i < size; i++) {
				var para = paragraphs.get(i);
				String text = para.getText();
				if (text == null) {
					text = "";
				}
				text = text.trim();
				if (text.isEmpty() || text.startsWith("#") || isListItem(text) || text.startsWith(">")) {
					break;
				}
				range.endParagraph = i;
			}
			// Ensures the range is at least one line
			if (range.endParagraph == range.startParagraph) {
				range.endParagraph = Math.min(range.startParagraph + 1, size - 1);
			}
		}

		return range;
	}

	/** Gets the header level */
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

	/** Checks if the text is a list item */
	private boolean isListItem(String text) {
		return text.matches("^\\s*[-*+]\\s+.*") || text.matches("^\\s*\\d+\\.\\s+.*");
	}

	/** Checks if the paragraph is folded */
	private boolean isFolded(int paragraphIndex) {
		return foldedParagraphs.contains(paragraphIndex);
	}

	/** Updates folding indicators */
	private void updateFoldingIndicators() {
		// Displays folding indicators next to line numbers
		// Example implementation: custom drawing of line numbers to show folding state
		// Currently simple implementation with log output
		System.out.println("Folded paragraphs: " + foldedParagraphs);
	}

	/** Represents a range of paragraphs */
	private static class ParagraphRange {
		private int startParagraph;
		private int endParagraph;

		public int getStartParagraph() {
			return startParagraph;
		}

		public void setStartParagraph(int startParagraph) {
			this.startParagraph = startParagraph;
		}

		public int getEndParagraph() {
			return endParagraph;
		}

		public void setEndParagraph(int endParagraph) {
			this.endParagraph = endParagraph;
		}
	}

	/** Clears all folding states */
	public void clearAllFolding() {
		for (int paragraph : new HashSet<>(foldedParagraphs)) {
			expandParagraph(paragraph);
		}
	}

	/** Gets the list of folded paragraphs */
	public Set<Integer> getFoldedParagraphs() {
		return new HashSet<>(foldedParagraphs);
	}
}
