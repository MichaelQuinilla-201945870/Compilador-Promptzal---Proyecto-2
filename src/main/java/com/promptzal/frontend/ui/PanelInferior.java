package com.promptzal.frontend.ui;

import com.promptzal.frontend.tema.Iconos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

// Panel de abajo: una pestaña por resultado de la compilacion
class PanelInferior extends StackPane {

    PanelInferior(Runnable alOcultar) {
        TabPane pestanas = new TabPane(
                pestana("Errores", Iconos.ERRORES, "Compila el archivo con Shift+F10 para ver los errores."),
                pestana("Tokens", Iconos.TOKENS, "Compila el archivo para ver sus tokens."),
                pestana("Símbolos", Iconos.SIMBOLOS, "Compila el archivo para ver la tabla de símbolos."),
                pestana("AST", Iconos.AST, "Compila el archivo para ver su árbol de sintaxis."));
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Button ocultar = Componentes.botonIcono(Iconos.OCULTAR, "Ocultar el panel");
        ocultar.setOnAction(e -> alOcultar.run());
        StackPane.setAlignment(ocultar, Pos.TOP_RIGHT);
        StackPane.setMargin(ocultar, new Insets(3, 8, 0, 0));

        getChildren().addAll(pestanas, ocultar);
        setMinHeight(120);
    }

    private static Tab pestana(String titulo, String icono, String mensajeVacio) {
        Tab tab = new Tab(titulo, Componentes.estadoVacio(mensajeVacio));
        tab.setGraphic(Iconos.crear(icono));
        tab.setClosable(false);
        return tab;
    }
}