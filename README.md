# Shiori Editor — Markdown対応日本語テキストエディタ

Java 17 / JavaFX ベースの軽量テキストエディタです。Markdown のプレビューやシンタックスハイライト、日本語環境に配慮した操作性を提供します。

## 特長
- Markdown 解析: FlexMark による高速・高精度パース
- 編集体験: RichTextFX によるリッチテキスト編集、段落折りたたみ
- 表示/テーマ: CSS ベースのテーマ、Markdown/プログラミングの強調表示

## 必要要件
- JDK 17 以上
- Maven 3.8 以上（`mvn -v` で確認）

## 使い方（ローカル実行）
```bash
mvn javafx:run
```

## ビルド
```bash
mvn clean package
```
生成物は `target/` 配下へ出力されます。

## テスト & 整形
```bash
mvn test                # JUnit 5
mvn spotless:apply      # google-java-format による自動整形
```
pre-commit フックが同梱されており、コミット時に整形とテストを自動実行します。

## 開発に参加する
バグ報告・機能提案は Issue を、変更提案は Pull Request をご利用ください。詳細は以下を参照してください。
- CONTRIBUTING.md（フロー/規約）
- AGENTS.md（プロジェクト規約・詳細ガイド）

## ライセンス
Apache License 2.0 の下で提供します。詳細は `LICENSE` を参照してください。
