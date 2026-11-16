package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("primary.fxml"));
        Parent root = loader.load();

        stage.setTitle("Gestor de Tareas - Grupo 1");
        stage.setScene(new Scene(root, 950, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}