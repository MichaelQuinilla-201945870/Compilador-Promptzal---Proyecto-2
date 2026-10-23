package com.promptzal.frontend.ui;

import com.promptzal.frontend.tema.Iconos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

// Barra de debajo del menú: nombre del proyecto y acciones rapidas
class BarraHerramientas extends HBox {

    BarraHerramientas(AreaEditor editor) {
        Label proyecto = new Label("promptzal");
        proyecto.getStyleClass().add("nombre-proyecto");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Button abrir = Componentes.botonIcono(Iconos.ABRIR, "Abrir archivo (Ctrl+O)");
        abrir.setOnAction(e -> editor.abrir());

        Button guardar = Componentes.botonIcono(Iconos.GUARDAR, "Guardar (Ctrl+S)");
        guardar.setOnAction(e -> editor.guardar());
        guardar.disableProperty().bind(editor.hayDocumentoProperty().not());

        Button compilar = new Button("Compilar", Iconos.crear(Iconos.COMPILAR));
        compilar.getStyleClass().add("boton-compilar");
        compilar.setTooltip(new Tooltip("Compilar (Shift+F10)"));
        compilar.setDisable(true);                   // se activa en F4

        getChildren().addAll(proyecto, espacio, abrir, guardar, compilar);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8);
        getStyleClass().add("barra-herramientas");
    }
}