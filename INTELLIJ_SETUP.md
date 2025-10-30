# IntelliJ IDEAでのセットアップ手順

このプロジェクトをIntelliJ IDEAで開いて開発するための手順です。

## 前提条件

- IntelliJ IDEA 2023.1以降（KotlinとCompose Desktopのサポートが必要）
- JDK 21以上
- Maven 3.8以上（またはMaven Wrapperを使用）

## プロジェクトを開く

1. **IntelliJ IDEAを起動**

2. **プロジェクトを開く**
   - `File` > `Open...`を選択
   - プロジェクトのルートディレクトリ（`pom.xml`があるディレクトリ）を選択
   - `Open`をクリック

3. **Mavenプロジェクトとして認識**
   - IntelliJ IDEAが自動的にMavenプロジェクトとして認識します
   - 右下に通知が表示された場合、`Import Maven Project`をクリック
   - 依存関係のダウンロードが自動的に開始されます

## 初回セットアップ

### 1. JDKの設定

1. `File` > `Project Structure` (⌘; / Ctrl+Alt+Shift+S)
2. `Project`タブを選択
3. `Project SDK`でJDK 21を選択（まだ設定されていない場合）
4. `Project language level`を`21`に設定
5. `Apply` > `OK`

### 2. Maven設定の確認

1. `File` > `Settings` (⌘, / Ctrl+Alt+S)
2. `Build, Execution, Deployment` > `Build Tools` > `Maven`
3. 以下を確認：
   - `Maven home path`: システムのMaven、またはMaven Wrapper
   - `User settings file`: Maven設定ファイルのパス
   - `Local repository`: ローカルリポジトリのパス

### 3. Kotlinプラグインの確認

1. `File` > `Settings` > `Plugins`
2. `Kotlin`プラグインが有効になっていることを確認
3. 無効な場合は有効化してIntelliJ IDEAを再起動

### 4. Compose Desktopプラグイン（オプション）

Compose Desktop向けの機能を使用する場合：
1. `File` > `Settings` > `Plugins`
2. `Compose Multiplatform`プラグインを検索してインストール（利用可能な場合）

## 実行設定

### アプリケーションの実行

1. `src/main/kotlin/com/texteditor/Main.kt`を開く
2. `main`関数の横にある実行ボタン（▶）をクリック
3. または、右クリック > `Run 'MainKt'`

### 実行設定の作成（カスタマイズ）

1. `Run` > `Edit Configurations...`
2. `+` > `Application`を選択
3. 以下を設定：
   - **Name**: `Shiori Editor`
   - **Main class**: `com.texteditor.MainKt`
   - **Use classpath of module**: `markdown-text-editor.main`
   - **JRE**: `21`（Project SDK）
4. `Apply` > `OK`

## ビルドと実行

### Mavenコマンドを使用

ターミナルで以下を実行：

```bash
# 依存関係のダウンロードとコンパイル
./mvnw clean compile

# アプリケーションの実行
./mvnw compose:desktop:run

# JARファイルの作成
./mvnw clean package
```

### IntelliJ IDEAのMavenツールウィンドウを使用

1. 右側の`Maven`タブを開く
2. `markdown-text-editor` > `Lifecycle`を展開
3. 実行したいタスクをダブルクリック：
   - `clean`: ビルド成果物を削除
   - `compile`: コンパイル
   - `package`: JARファイルを作成
   - `test`: テストを実行

## コードフォーマット

プロジェクトではSpotlessを使用してコードフォーマットを管理しています。

### IntelliJ IDEAでのフォーマット設定

1. `File` > `Settings` > `Editor` > `Code Style` > `Kotlin`
2. `Set from...` > `Predefined style` > `Kotlin style guide`を選択
3. または、プロジェクトの`.editorconfig`ファイルがある場合は、それを使用

### Spotlessでフォーマット

```bash
# フォーマットの確認
./mvnw spotless:check

# フォーマットの適用
./mvnw spotless:apply
```

## デバッグ

1. `Main.kt`の`main`関数にブレークポイントを設定
2. 実行ボタンの横の`Debug`をクリック
3. または、右クリック > `Debug 'MainKt'`

## トラブルシューティング

### 依存関係が解決されない

1. `File` > `Invalidate Caches...`
2. `Invalidate and Restart`を選択
3. Mavenツールウィンドウで`Reload All Maven Projects`をクリック

### Compose Desktopのクラスが見つからない

1. `File` > `Project Structure` > `Libraries`
2. Compose Desktopの依存関係が正しく追加されているか確認
3. Mavenツールウィンドウで`Reload All Maven Projects`をクリック

### Kotlinコードが認識されない

1. `File` > `Project Structure` > `Modules`
2. `markdown-text-editor.main`モジュールで`src/main/kotlin`が`Sources`として認識されているか確認
3. 認識されていない場合、フォルダを右クリック > `Mark Directory as` > `Sources Root`

## 便利な機能

### Code Completion

IntelliJ IDEAはKotlinとCompose Desktopの完全なコード補完をサポートしています。

### Compose Preview（利用可能な場合）

Compose Multiplatformプラグインがインストールされている場合、`@Preview`アノテーションを使用してUIコンポーネントをプレビューできます。

### リファクタリング

IntelliJ IDEAの強力なリファクタリング機能を使用して、Kotlinコードを安全にリファクタリングできます。

## 参考リンク

- [IntelliJ IDEA Documentation](https://www.jetbrains.com/help/idea/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Compose Desktop Documentation](https://www.jetbrains.com/lp/compose-multiplatform/)