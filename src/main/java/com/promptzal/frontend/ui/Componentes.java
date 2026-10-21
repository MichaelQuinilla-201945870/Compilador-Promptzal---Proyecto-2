package com.promptzal.frontend.ui;

import com.promptzal.frontend.tema.Iconos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

// Piezas reutilizables: encabezado de panel, boton de icono y mensaje de panel vacio
final class Componentes {

    private Componentes() {
    }

    // Titulo a la izquierda y botones de accion a la derecha
    static HBox encabezado(String titulo, Node... acciones) {
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("encabezado-titulo");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox caja = new HBox(4, etiqueta, espacio);
        caja.getChildren().addAll(acciones);
        caja.setAlignment(Pos.CENTER_LEFT);
        caja.getStyleClass().add("encabezado-panel");
        return caja;
    }

    // Boton que solo muestra un icono: lleva descripcion para el tooltip y para lectores de pantalla.
    static Button botonIcono(String codigoIcono, String descripcion) {
        Button boton = new Button();
        boton.setGraphic(Iconos.crear(codigoIcono));
        boton.getStyleClass().add("boton-icono");
        boton.setTooltip(new Tooltip(descripcion));
        boton.setAccessibleText(descripcion);
        return boton;
    }

    // Mensaje de un panel sin contenido: dice que hacer para llenarlo.
    static Node estadoVacio(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("estado-vacio");
        etiqueta.setWrapText(true);

        StackPane contenedor = new StackPane(etiqueta);
        contenedor.setPadding(new Insets(16));
        return contenedor;
    }
}