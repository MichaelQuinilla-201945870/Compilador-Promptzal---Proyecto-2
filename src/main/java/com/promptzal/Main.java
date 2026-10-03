package com.promptzal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("PromptZal");
        stage.setScene(new Scene(new StackPane(new Label("PromptZal V3")), 800, 500));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
