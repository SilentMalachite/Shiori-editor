# Repository Guidelines

## プロジェクト構成 & モジュール
- 技術スタック: Kotlin 1.9.22, Gradle, Jetpack Compose Desktop, FlexMark。
- 標準 Gradle レイアウト:
  - `src/main/kotlin` … アプリ本体（パッケージ: `com.texteditor.*`）
  - `src/main/resources` … リソース（例: `app.css`, `markdown-highlight.css`, `programming-highlight.css`）
  - `src/test/kotlin` … ユニットテスト配置
  - `build/` … ビルド成果物（編集不可）

## ビルド・実行・開発コマンド
- `./gradlew build` … 依存解決＋JAR作成。
- `./gradlew run` … ローカル起動（`com.texteditor.MainKt` を実行）。
- `./gradlew test` … テスト実行。
- `./gradlew spotlessCheck` … フォーマット検査。
- `./gradlew spotlessApply` … 自動整形を適用（Kotlin用ktfmt使用）。
- `./gradlew packageDmg` / `packageMsi` / `packageDeb` … ネイティブアプリケーション作成。
- 前提: JDK 21, Gradle 8.5+（Gradle Wrapper同梱）, Kotlin 1.9.22。

## コーディング規約・命名
- インデント: スペース2。UTF-8。行末に不要な空白を残さない。
- Kotlin 命名: クラス/インターフェースは `PascalCase`、関数/変数は `camelCase`、定数は `UPPER_SNAKE_CASE`。
- パッケージは `com.texteditor.*` を維持。UI リソースは `src/main/resources` に配置。
- フォーマッタ/リンター: Spotless + ktfmt 導入済み（未使用importは自動削除）。`./gradlew spotlessCheck` をCI、コミット前は `./gradlew spotlessApply` を推奨。

## テスト方針
- フレームワーク: JUnit 5（Jupiter）+ Kotlin Test 導入済み。
- 配置/命名: `src/test/kotlin` に `*Test.kt`。クラス単位で主要ロジックを網羅。
- 実行例: `./gradlew test`、個別は `./gradlew test --tests EditorServiceTest`。
- 変更点には回帰防止のテストを追加し、外部I/Oは可能ならモック化。

## コミット & プルリクエスト
- コミットは英語・命令形・短い概要（例: `Add Markdown preview toggle`）。本文に目的/背景と関連Issueを記載。
- PR 要件: 概要、動作確認手順、UI変更はスクリーンショット、関連Issueリンク。小さく独立したPRを推奨。
 - 事前チェック: `./gradlew spotlessApply` と `./gradlew test` を通してからコミット/プッシュ。

## Git Hooks（pre-commit）
- 目的: コミット前にフォーマットとテストを自動実行。
- 仕組み: `.githooks/pre-commit` を使用。リポジトリに同梱済み。
- 有効化: `git config core.hooksPath .githooks` を実行して有効化する必要あり。
- 実行内容:
  - `./gradlew spotlessApply`（差分が出た場合はステージングに追加）
  - `./gradlew test`（失敗時はコミット中断）

## セキュリティ & 設定
- 秘密情報や環境依存ファイルはコミットしない（例: APIキー、`build/`, `.gradle/`）。
- 依存追加は `build.gradle.kts` を最小限に保ち、バージョン整合性を確認。

## Agent向けメモ
- 本ファイルのスコープはリポジトリ全体。`build/`, `.gradle/` は書き換え禁止。
- 既存構成/命名を尊重し、不要なリネームを避ける。Kotlin 1.9.22 + JDK 21 + Gradle前提でビルド。
