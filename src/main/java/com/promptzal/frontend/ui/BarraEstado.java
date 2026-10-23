package com.promptzal.frontend.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.util.List;

// Barra inferior: resultado de la ultima accion a la izquierda, datos del editor a la derecha.
class BarraEstado extends HBox {

    private final Label mensaje = new Label("Listo");
    private final Label posicion = new Label("1:1");

    BarraEstado() {
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        posicion.setMinWidth(56);
        posicion.setAlignment(Pos.CENTER_RIGHT);

        Label codificacion = new Label("UTF-8");
        Label saltos = new Label("LF");
        Label tipo = new Label("PromptZal");
        for (Label etiqueta : List.of(posicion, codificacion, saltos, tipo)) {
            etiqueta.getStyleClass().add("texto-silenciado");
        }

        getChildren().addAll(mensaje, espacio, posicion, codificacion, saltos, tipo);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(16);
        getStyleClass().add("barra-estado");
    }

    void setMensaje(String texto) {
        mensaje.setText(texto);
    }

    void setPosicion(int linea, int columna) {
        posicion.setText(linea <= 0 ? "" : linea + ":" + columna);
    }
}