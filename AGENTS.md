# Repository Guidelines

## プロジェクト構成 & モジュール
- 技術スタック: Java 21, Maven, JavaFX, RichTextFX, FlexMark。
- 標準 Maven レイアウト:
  - `src/main/java` … アプリ本体（パッケージ: `com.texteditor.*`）
  - `src/main/resources` … リソース（例: `app.css`, `markdown-highlight.css`, `programming-highlight.css`）
  - `src/test/java` … ユニットテスト配置
  - `target/` … ビルド成果物（編集不可）

## ビルド・実行・開発コマンド
- `mvn clean package` … 依存解決＋JAR作成。
- `mvn javafx:run` … ローカル起動（`com.texteditor.TextEditor` を実行）。
- `mvn test` … テスト実行（未導入の場合は0件）。
- `mvn spotless:check` … フォーマット検査（`verify` フェーズでも実行）。
- `mvn spotless:apply` … 自動整形を適用。
- 前提: JDK 21, Maven 3.8+（`mvn -v` で確認）。

## コーディング規約・命名
- インデント: スペース2。UTF-8。行末に不要な空白を残さない。
- Java 命名: クラス/インターフェースは `PascalCase`、メソッド/変数は `camelCase`、定数は `UPPER_SNAKE_CASE`。
- パッケージは `com.texteditor.*` を維持。UI リソースは `src/main/resources` に配置。
- フォーマッタ/リンター: Spotless + Eclipse formatter 導入済み（未使用importは自動削除）。`mvn spotless:check` をCI/`verify`で、コミット前は `mvn spotless:apply` を推奨。

## テスト方針
- フレームワーク: JUnit 5（Jupiter）導入済み。Surefire 3.x で実行。
- 配置/命名: `src/test/java` に `*Test.java`。クラス単位で主要ロジックを網羅。
- 実行例: `mvn test`、個別は `mvn -Dtest=EditorServiceTest test`。
- 変更点には回帰防止のテストを追加し、外部I/Oは可能ならモック化。

## コミット & プルリクエスト
- コミットは英語・命令形・短い概要（例: `Add Markdown preview toggle`）。本文に目的/背景と関連Issueを記載。
- PR 要件: 概要、動作確認手順、UI変更はスクリーンショット、関連Issueリンク。小さく独立したPRを推奨。
 - 事前チェック: `mvn spotless:apply` と `mvn test` を通してからコミット/プッシュ。

## Git Hooks（pre-commit）
- 目的: コミット前にフォーマットとテストを自動実行。
- 仕組み: `.githooks/pre-commit` を使用。リポジトリに同梱済み。
- 有効化: すでに `core.hooksPath=.githooks` を設定済み。万一未設定の場合は `git config core.hooksPath .githooks` を実行。
- 実行内容:
  - `mvn spotless:apply`（差分が出た場合はステージングに追加）
  - `mvn test`（失敗時はコミット中断）

## セキュリティ & 設定
- 秘密情報や環境依存ファイルはコミットしない（例: APIキー、`target/`）。
- 依存追加は `pom.xml` を最小限に保ち、バージョン整合性を確認。

## Agent向けメモ
- 本ファイルのスコープはリポジトリ全体。`target/` は書き換え禁止。
- 既存構成/命名を尊重し、不要なリネームを避ける。Java 21 前提でビルド。
