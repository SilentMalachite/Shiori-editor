package com.texteditor;

import javafx.application.Application;
import javafx.stage.Stage;

/** アプリケーションのエントリーポイント TextEditorクラスを起動する */
public class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    // TextEditorクラスに処理を委譲
    TextEditor textEditor = new TextEditor();
    textEditor.start(primaryStage);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
