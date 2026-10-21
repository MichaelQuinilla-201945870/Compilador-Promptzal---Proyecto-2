package com.promptzal.frontend.ui;

import com.promptzal.frontend.tema.Iconos;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// Tira vertical con un boton por panel. Cada boton está atado a la visibilidad de su panel.
class BarraLateral extends VBox {

    private final Region espacio = new Region();

    BarraLateral(boolean izquierda) {
        getStyleClass().addAll("barra-lateral", izquierda ? "barra-lateral-izq" : "barra-lateral-der");
        VBox.setVgrow(espacio, Priority.ALWAYS);
        getChildren().add(espacio);
    }

    //Boton en la parte superior de la tira
    void arriba(String icono, String descripcion, BooleanProperty visible) {
        getChildren().add(getChildren().indexOf(espacio), crear(icono, descripcion, visible));
    }

    // Boton en la parte inferior de la tira
    void abajo(String icono, String descripcion, BooleanProperty visible) {
        getChildren().add(crear(icono, descripcion, visible));
    }

    private ToggleButton crear(String icono, String descripcion, BooleanProperty visible) {
        ToggleButton boton = new ToggleButton();
        boton.setGraphic(Iconos.crear(icono));
        boton.getStyleClass().add("boton-herramienta");
        boton.setTooltip(new Tooltip(descripcion));
        boton.setAccessibleText(descripcion);
        boton.selectedProperty().bindBidirectional(visible);
        return boton;
    }
}