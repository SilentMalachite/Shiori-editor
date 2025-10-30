# Shiori Editor — Markdown対応日本語テキストエディタ

Kotlin / Jetpack Compose Desktop ベースの軽量テキストエディタです。Markdown のプレビューやシンタックスハイライト、日本語環境に配慮した操作性を提供します。

## 特長

### 編集機能
- **Markdown 構文ハイライト**: 見出し、リスト、リンク、コードブロックなどのMarkdown要素を強調表示
- **プログラミング言語サポート**: Java, Go, C/C++, Haskell の構文ハイライト（Markdown, Plain Text も対応）
- **段落折りたたみ**: Ctrl + Enter を押しながらマウスクリックで段落を展開/折りたたみ
- **リッチテキスト編集**: Jetpack Compose Desktop を使用したモダンなUI
- **行番号表示**: 編集画面の左側に行番号を表示
- **タブインデント**: Tabキー/Shift+Tab で4スペースのインデント操作
- **日本語フォント対応**: Yu Gothic UI, Meiryo, MS Gothic を使用した日本語表示最適化

### 表示機能
- **Markdown プレビュー**: FlexMark によるライブHTMLプレビュー（表示メニューから切替）
- **ダーク/ライトテーマ切り替え**: 表示メニューのダークモードチェックボックスでテーマ変更
- **Material Design**: Compose Material 3 によるモダンなUIデザイン

### 操作性
- **ファイル操作**: 新規作成、開く、保存、名前を付けて保存
- **編集機能**: 元に戻す、やり直し、切り取り、コピー、貼り付け、全選択
- **ツールバー**: 新規、開く、保存、太字、斜体、コードの挿入ボタン
- **言語切り替え**: ツールバーのコンボボックスで編集言語を選択
- **UTF-8エンコーディング**: ファイルの読み書きをUTF-8で完全対応

## 必要要件
- JDK 21 以上
- Kotlin 1.9.22
- Gradle 8.5 以上（Gradle Wrapperを同梱）

## IntelliJ IDEAで開く

このプロジェクトはIntelliJ IDEAで直接開くことができます。

1. IntelliJ IDEAを起動
2. `File` > `Open...` でプロジェクトのルートディレクトリ（`build.gradle.kts`があるディレクトリ）を選択
3. Gradleプロジェクトとして自動認識されます

詳細なセットアップ手順は [INTELLIJ_SETUP.md](INTELLIJ_SETUP.md) を参照してください。

## 使い方（ローカル実行）

### Gradle Wrapperを使用（推奨）
```bash
# macOS / Linux
./gradlew run

# Windows
gradlew.bat run
```

### Gradleがインストールされている場合
```bash
gradle run
```

## ビルド

```bash
# Gradle Wrapperを使用
./gradlew build

# Gradleがインストールされている場合
gradle build
```

生成物は `build/libs/` 配下へ出力されます。

## ネイティブアプリケーションの作成

```bash
# macOS用DMG作成
./gradlew packageDmg

# Windows用MSI作成
./gradlew packageMsi

# Linux用DEB作成
./gradlew packageDeb
```

## テスト & 整形

```bash
# テスト実行
./gradlew test

# コードフォーマット（Kotlin用ktfmt）
./gradlew spotlessApply

# フォーマットチェック
./gradlew spotlessCheck
```

### テストカバレッジ

プロジェクトには89個のユニットテストが含まれています：

- **ErrorHandler**: 9テスト - エラーハンドリングとログ機能
- **ParagraphFoldingManager**: 17テスト - 段落折りたたみ機能
- **ProgrammingLanguageSupport**: 17テスト - シンタックスハイライト（Java, Go, C++, Haskell）
- **MarkdownHighlighter**: 22テスト - Markdown構文ハイライト
- **ThemeManager**: 5テスト - テーマ切り替え機能
- **TextEditorUtils**: 19テスト - テキスト編集ユーティリティ関数

すべてのテストは `src/test/kotlin/com/texteditor/` に配置されています。

pre-commit フックが同梱されており、コミット時に整形とテストを自動実行します。
有効化するには: `git config core.hooksPath .githooks`

## 操作のヒント
- **太字/斜体/コード**: ツールバーの `B` / `I` / `Code` ボタンで選択範囲を囲めます。
- **段落折りたたみ**: Ctrl(⌘) + Enter を押しながら段落をクリック。
- **テーマ切り替え**: 表示メニューの「ダークモード」チェックボックスで切替。
- **言語切り替え**: ツールバーの言語コンボボックスから選択。
- **Markdownプレビュー**: 表示メニューの「Markdownプレビュー」チェックボックスで切替。

## 開発に参加する
バグ報告・機能提案は Issue を、変更提案は Pull Request をご利用ください。詳細は以下を参照してください。
- [CONTRIBUTING.md](CONTRIBUTING.md)（フロー/規約）
- [AGENTS.md](AGENTS.md)（プロジェクト規約・詳細ガイド）

## ライセンス
Apache License 2.0 の下で提供します。詳細は `LICENSE` を参照してください。
