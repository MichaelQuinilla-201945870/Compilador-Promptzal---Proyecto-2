package com.promptzal.frontend.ui;

import com.promptzal.frontend.tema.Iconos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

class PanelPromptzal extends BorderPane {

    PanelPromptzal(Runnable alOcultar) {
        Button copiar = Componentes.botonIcono(Iconos.COPIAR, "Copiar el prompt");
        Button guardar = Componentes.botonIcono(Iconos.GUARDAR, "Guardar como…");
        Button ocultar = Componentes.botonIcono(Iconos.OCULTAR, "Ocultar el panel");
        ocultar.setOnAction(e -> alOcultar.run());

        // Se activan cuando exista un prompt generado
        copiar.setDisable(true);
        guardar.setDisable(true);

        setTop(Componentes.encabezado("Promptzal", copiar, guardar, ocultar));
        setCenter(Componentes.estadoVacio("Compila el archivo y el prompt generado aparecerá aquí."));
        getStyleClass().add("panel-promptzal");
        setMinWidth(240);
    }
}