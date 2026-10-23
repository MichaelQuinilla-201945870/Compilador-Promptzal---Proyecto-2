package com.promptzal.frontend.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

// Ventanas de confirmacion y de error con el tema de la aplicacion
final class Dialogos {

    enum Respuesta { GUARDAR, DESCARTAR, CANCELAR }

    private Dialogos() {
    }

    static Respuesta confirmarCambios(Window dueno, String archivo) {
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.YES);
        ButtonType descartar = new ButtonType("No guardar", ButtonBar.ButtonData.NO);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alerta = new Alert(Alert.AlertType.NONE,
                "Si no los guardas, se perderán los cambios.", guardar, descartar, cancelar);
        alerta.setTitle("Cambios sin guardar");
        alerta.setHeaderText("¿Guardar los cambios en " + archivo + "?");
        aplicarTema(alerta, dueno);

        ButtonType elegido = alerta.showAndWait().orElse(cancelar);
        if (elegido == guardar) {
            return Respuesta.GUARDAR;
        }
        if (elegido == descartar) {
            return Respuesta.DESCARTAR;
        }
        return Respuesta.CANCELAR;
    }

    static void error(Window dueno, String titulo, String detalle) {
        Alert alerta = new Alert(Alert.AlertType.NONE, detalle, ButtonType.OK);
        alerta.setTitle("Error");
        alerta.setHeaderText(titulo);
        aplicarTema(alerta, dueno);
        alerta.showAndWait();
    }

    private static void aplicarTema(Alert alerta, Window dueno) {
        if (dueno != null) {
            alerta.initOwner(dueno);
            if (dueno.getScene() != null) {
                alerta.getDialogPane().getStylesheets().addAll(dueno.getScene().getStylesheets());
            }
        }
    }
}