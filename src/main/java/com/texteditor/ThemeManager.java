package com.texteditor;

import javafx.scene.Scene;

/**
 * テーマ管理クラス
 * ダークモードとライトモードの切り替えを管理
 */
public class ThemeManager {
    
    private boolean isDarkMode = false;
    
    /**
     * 現在のシステムテーマに基づいて初期テーマを設定
     */
    public ThemeManager() {
        // OSのテーマ検出（簡易実装）
        String osTheme = System.getProperty("os.name").toLowerCase();
        // Linuxの場合のテーマ検出は複雑なので、デフォルトはライトテーマ
        this.isDarkMode = false;
    }
    
    /**
     * シーンにテーマを適用
     */
    public void applyTheme(Scene scene) {
        if (isDarkMode) {
            applyDarkTheme(scene);
        } else {
            applyLightTheme(scene);
        }
    }
    
    /**
     * テーマを切り替え
     */
    public void toggleTheme(Scene scene) {
        isDarkMode = !isDarkMode;
        applyTheme(scene);
    }
    
    /**
     * ダークテーマを適用
     */
    private void applyDarkTheme(Scene scene) {
        String darkStyle = 
            "/* ダークテーマCSS */ " +
            ".root { " +
            "    -fx-base: #2b2b2b; " +
            "    -fx-background: #2b2b2b; " +
            "    -fx-control-inner-background: #1e1e1e; " +
            "    -fx-control-inner-background-alt: #262626; " +
            "    -fx-accent: #0096C9; " +
            "    -fx-focus-color: #0096C9; " +
            "    -fx-text-fill: #f0f0f0; " +
            "    -fx-text-inner-color: #f0f0f0; " +
            "} " +
            
            ".code-area { " +
            "    -fx-background-color: #1e1e1e; " +
            "    -fx-text-fill: #f0f0f0; " +
            "} " +
            
            ".line-number { " +
            "    -fx-background-color: #2b2b2b; " +
            "    -fx-text-fill: #858585; " +
            "} " +
            
            ".menu-bar { " +
            "    -fx-background-color: #2b2b2b; " +
            "} " +
            
            ".tool-bar { " +
            "    -fx-background-color: #2b2b2b; " +
            "} " +
            
            ".button { " +
            "    -fx-background-color: #404040; " +
            "    -fx-text-fill: #f0f0f0; " +
            "    -fx-border-color: #555555; " +
            "} " +
            
            ".button:hover { " +
            "    -fx-background-color: #505050; " +
            "} ";
        
        scene.getRoot().setStyle(darkStyle);
    }
    
    /**
     * ライトテーマ（"ライトJa"テーマ）を適用
     */
    private void applyLightTheme(Scene scene) {
        String lightStyle = 
            "/* ライトJaテーマCSS */ " +
            ".root { " +
            "    -fx-base: #ffffff; " +
            "    -fx-background: #ffffff; " +
            "    -fx-control-inner-background: #ffffff; " +
            "    -fx-control-inner-background-alt: #f5f5f5; " +
            "    -fx-accent: #0078d4; " +
            "    -fx-focus-color: #0078d4; " +
            "    -fx-text-fill: #323130; " +
            "    -fx-text-inner-color: #323130; " +
            "} " +
            
            ".code-area { " +
            "    -fx-background-color: #ffffff; " +
            "    -fx-text-fill: #323130; " +
            "} " +
            
            ".line-number { " +
            "    -fx-background-color: #f8f8f8; " +
            "    -fx-text-fill: #6f6f6f; " +
            "} " +
            
            ".menu-bar { " +
            "    -fx-background-color: #f8f8f8; " +
            "} " +
            
            ".tool-bar { " +
            "    -fx-background-color: #f8f8f8; " +
            "} " +
            
            ".button { " +
            "    -fx-background-color: #ffffff; " +
            "    -fx-text-fill: #323130; " +
            "    -fx-border-color: #d1d1d1; " +
            "} " +
            
            ".button:hover { " +
            "    -fx-background-color: #f0f0f0; " +
            "} ";
        
        scene.getRoot().setStyle(lightStyle);
    }
    
    /**
     * 現在のテーマがダークモードかどうか
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }
}