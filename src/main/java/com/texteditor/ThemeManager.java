package com.texteditor;

import javafx.scene.Parent;
import javafx.scene.Scene;

/** テーマ管理クラス ダークモードとライトモードの切り替えを管理 */
public class ThemeManager {

  private boolean isDarkMode = false;

  /** 現在のシステムテーマに基づいて初期テーマを設定 */
  public ThemeManager() {
    // デフォルトはライトテーマ（将来的にOS検出に拡張可能）
    this.isDarkMode = false;
  }

  /** シーンにテーマを適用 */
  public void applyTheme(Scene scene) {
    if (scene == null || scene.getRoot() == null) return;
    if (isDarkMode) {
      applyDarkTheme(scene);
    } else {
      applyLightTheme(scene);
    }
  }

  /** テーマを切り替え */
  public void toggleTheme(Scene scene) {
    isDarkMode = !isDarkMode;
    applyTheme(scene);
  }

  /** ダークテーマを適用 ルートに "dark-theme" スタイルクラスを付与/維持する */
  private void applyDarkTheme(Scene scene) {
    Parent root = scene.getRoot();
    root.getStyleClass().add("dark-theme");
  }

  /** ライトテーマ（"ライトJa"テーマ）を適用 ルートから "dark-theme" スタイルクラスを除去する */
  private void applyLightTheme(Scene scene) {
    Parent root = scene.getRoot();
    root.getStyleClass().remove("dark-theme");
  }

  /** 現在のテーマがダークモードかどうか */
  public boolean isDarkMode() {
    return isDarkMode;
  }
}
