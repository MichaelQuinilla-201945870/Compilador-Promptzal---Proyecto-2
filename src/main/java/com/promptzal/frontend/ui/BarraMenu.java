package com.promptzal.frontend.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

// Menú principal.( aun faltan elementos por activar)
class BarraMenu extends MenuBar {

    BarraMenu(BooleanProperty proyecto, BooleanProperty promptzal, BooleanProperty inferior,
              AreaEditor editor, Runnable compilar, Runnable salir) {

        ReadOnlyBooleanProperty hayDocumento = editor.hayDocumentoProperty();

        getMenus().addAll(
                new Menu("Archivo", null,
                        accion("Nuevo", ctrl(KeyCode.N), editor::nuevo),
                        accion("Abrir…", ctrl(KeyCode.O), editor::abrir),
                        pendiente("Abrir carpeta…", ctrlShift(KeyCode.O)),
                        new SeparatorMenuItem(),
                        accion("Guardar", ctrl(KeyCode.S), editor::guardar, hayDocumento),
                        accion("Guardar como…", ctrlShift(KeyCode.S), editor::guardarComo, hayDocumento),
                        new SeparatorMenuItem(),
                        accion("Cerrar pestaña", ctrl(KeyCode.W), editor::cerrarActiva, hayDocumento),
                        new SeparatorMenuItem(),
                        accion("Salir", ctrl(KeyCode.Q), salir)),

                new Menu("Editar", null,
                        accion("Deshacer", null, editor::deshacer, hayDocumento),
                        accion("Rehacer", null, editor::rehacer, hayDocumento),
                        new SeparatorMenuItem(),
                        accion("Cortar", null, editor::cortar, hayDocumento),
                        accion("Copiar", null, editor::copiar, hayDocumento),
                        accion("Pegar", null, editor::pegar, hayDocumento),
                        new SeparatorMenuItem(),
                        pendiente("Comentar línea", ctrl(KeyCode.SLASH))),

                new Menu("Ver", null,
                        visibilidad("Proyecto", proyecto, alt(KeyCode.DIGIT1)),
                        visibilidad("Promptzal", promptzal, alt(KeyCode.DIGIT2)),
                        visibilidad("Errores y resultados", inferior, alt(KeyCode.DIGIT3))),

                new Menu("Compilar", null,
                        accion("Compilar", new KeyCodeCombination(KeyCode.F10, KeyCombination.SHIFT_DOWN),
                                compilar, hayDocumento)),

                new Menu("Reportes", null,
                        pendiente("Reporte de errores", null),
                        pendiente("Tabla de símbolos", null),
                        pendiente("Árbol de sintaxis (AST)", null)),

                new Menu("Ayuda", null,
                        pendiente("Acerca de PromptZal", null)));
    }

    // Elemento siempre activo.
    private static MenuItem accion(String texto, KeyCombination atajo, Runnable accion) {
        MenuItem item = new MenuItem(texto);
        if (atajo != null) {
            item.setAccelerator(atajo);
        }
        item.setOnAction(e -> accion.run());
        return item;
    }

    // elemento que solo esta activo mientras la condicion sea verdadera
    private static MenuItem accion(String texto, KeyCombination atajo, Runnable accion,
                                   ReadOnlyBooleanProperty habilitado) {
        MenuItem item = accion(texto, atajo, accion);
        item.disableProperty().bind(habilitado.not());
        return item;
    }

    private static CheckMenuItem visibilidad(String texto, BooleanProperty visible, KeyCombination atajo) {
        CheckMenuItem item = new CheckMenuItem(texto);
        item.selectedProperty().bindBidirectional(visible);   // menu, boton lateral y panel comparten el estado
        item.setAccelerator(atajo);
        return item;
    }

    // elemento que todavía no hace nada
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