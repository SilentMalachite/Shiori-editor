# Contributing to Shiori Editor

ご協力ありがとうございます。以下の手順・規約に従ってコントリビュートしてください。

## 開発フロー
- Issue を検索し、重複がなければ作成（バグ/提案はテンプレート利用）。
- フォーク or ブランチ作成（例: `feat/markdown-preview`, `fix/crash-on-startup`）。
- 実装 → `./gradlew spotlessApply && ./gradlew test` → コミット → PR 作成。

## コーディング規約
- Kotlin 1.9.22 + JDK 21、ktfmt formatter（Spotless + ktfmt）
- インデント: 2スペース、未使用 import は削除。
- パッケージ: `com.texteditor.*` を維持。UI リソースは `src/main/resources`。

## テスト
- JUnit 5（Jupiter）+ Kotlin Test。`src/test/kotlin` に `*Test.kt`。
- コアロジックにはユニットテストを追加。I/O は可能な限りモック化。

## コミット/PR
- コミットは英語・命令形（例: `Add Markdown preview toggle`）。
- PR には概要、動作確認手順、関連 Issue、UI 変更のスクリーンショットを含める。
- 小さく独立した PR を推奨。レビューコメントには素早く対応。

詳細は AGENTS.md を参照してください。
