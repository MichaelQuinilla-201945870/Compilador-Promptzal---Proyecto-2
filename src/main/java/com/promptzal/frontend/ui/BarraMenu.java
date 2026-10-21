package com.promptzal.frontend.ui;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

// Menu principal
class BarraMenu extends MenuBar {

    BarraMenu(BooleanProperty proyecto, BooleanProperty promptzal, BooleanProperty inferior) {
        getMenus().addAll(
                new Menu("Archivo", null,
                        pendiente("Nuevo", ctrl(KeyCode.N)),
                        pendiente("Abrir…", ctrl(KeyCode.O)),
                        pendiente("Abrir carpeta…", ctrlShift(KeyCode.O)),
                        new SeparatorMenuItem(),
                        pendiente("Guardar", ctrl(KeyCode.S)),
                        pendiente("Guardar como…", ctrlShift(KeyCode.S)),
                        new SeparatorMenuItem(),
                        salir()),
                new Menu("Editar", null,
                        pendiente("Deshacer", null),
                        pendiente("Rehacer", null),
                        new SeparatorMenuItem(),
                        pendiente("Cortar", null),
                        pendiente("Copiar", null),
                        pendiente("Pegar", null),
                        new SeparatorMenuItem(),
                        pendiente("Comentar línea", ctrl(KeyCode.SLASH))),
                new Menu("Ver", null,
                        visibilidad("Proyecto", proyecto, alt(KeyCode.DIGIT1)),
                        visibilidad("Promptzal", promptzal, alt(KeyCode.DIGIT2)),
                        visibilidad("Errores y resultados", inferior, alt(KeyCode.DIGIT3))),
                new Menu("Compilar", null,
                        pendiente("Compilar", new KeyCodeCombination(KeyCode.F10, KeyCombination.SHIFT_DOWN))),
                new Menu("Reportes", null,
                        pendiente("Reporte de errores", null),
                        pendiente("Tabla de símbolos", null),
                        pendiente("Árbol de sintaxis (AST)", null)),
                new Menu("Ayuda", null,
                        pendiente("Acerca de PromptZal", null)));
    }

    private static MenuItem salir() {
        MenuItem salir = new MenuItem("Salir");
        salir.setAccelerator(ctrl(KeyCode.Q));
        salir.setOnAction(e -> Platform.exit());
        return salir;
    }

    private static CheckMenuItem visibilidad(String texto, BooleanProperty visible, KeyCombination atajo) {
        CheckMenuItem item = new CheckMenuItem(texto);
        item.selectedProperty().bindBidirectional(visible);   // menú, botón lateral y panel comparten el estado
        item.setAccelerator(atajo);
        return item;
    }

    // Elemento que todavía no hace nada
    private static MenuItem pendiente(String texto, KeyCombination atajo) {
        MenuItem item = new MenuItem(texto);
        if (atajo != null) {
            item.setAccelerator(atajo);
        }
        item.setDisable(true);
        return item;
    }

    private static KeyCombination ctrl(KeyCode codigo) {
        return new KeyCodeCombination(codigo, KeyCombination.CONTROL_DOWN);
    }

    private static KeyCombination ctrlShift(KeyCode codigo) {
        return new KeyCodeCombination(codigo, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    }

    private static KeyCombination alt(KeyCode codigo) {
        return new KeyCodeCombination(codigo, KeyCombination.ALT_DOWN);
    }
}