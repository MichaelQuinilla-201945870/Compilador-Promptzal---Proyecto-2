package com.promptzal.frontend.ui;

import com.promptzal.frontend.tema.Iconos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

class PanelProyecto extends BorderPane {

    PanelProyecto(Runnable alOcultar) {
        Button ocultar = Componentes.botonIcono(Iconos.OCULTAR, "Ocultar el panel");
        ocultar.setOnAction(e -> alOcultar.run());

        setTop(Componentes.encabezado("Proyecto", ocultar));
        setCenter(Componentes.estadoVacio("Abre una carpeta con Ctrl+Shift+O para ver sus archivos."));
        getStyleClass().add("panel");
        setMinWidth(160);
    }
}