package com.promptzal.frontend;

import com.promptzal.frontend.tema.Fuentes;
import com.promptzal.frontend.ui.VentanaPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class AplicacionPromptZal extends Application {

    private static final String TEMA = "/com/promptzal/frontend/tema/oscuro.css";

    @Override
    public void start(Stage stage) {
        Fuentes.cargar();                       // antes de crear cualquier control
        System.out.println("JavaFX " + System.getProperty("javafx.runtime.version")
                + ", Java " + System.getProperty("java.version"));

        URL tema = getClass().getResource(TEMA);
        if (tema == null) {
            throw new IllegalStateException("No se encontró el tema " + TEMA
                    + ". Revisa que oscuro.css esté en src/main/resources/com/promptzal/frontend/tema/");
        }

        VentanaPrincipal ventana = new VentanaPrincipal();
        Scene escena = new Scene(ventana.getRaiz(), 1200, 720);
        escena.getStylesheets().add(tema.toExternalForm());

        stage.setTitle("PromptZal");
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setScene(escena);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}