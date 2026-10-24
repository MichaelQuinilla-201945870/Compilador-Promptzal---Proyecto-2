package com.promptzal.frontend.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

// partes comunes de las tablas del panel inferior.
final class Tablas {

    private Tablas() {
    }

    // Tabla con el mensaje que se ve cuando no hay filas.
    static <T> TableView<T> crear(Label marcador) {
        TableView<T> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        marcador.getStyleClass().add("estado-vacio");
        tabla.setPlaceholder(marcador);
        return tabla;
    }

    static <T> TableColumn<T, String> texto(String titulo, double ancho, Function<T, String> valor) {
        TableColumn<T, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(c -> new SimpleStringProperty(valor.apply(c.getValue())));
        columna.setPrefWidth(ancho);
        columna.setSortable(false);
        return columna;
    }

    // Columna numerica, alineada a la derecha
    static <T> TableColumn<T, Number> numero(String titulo, double ancho, ToIntFunction<T> valor) {
        TableColumn<T, Number> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(c -> new SimpleIntegerProperty(valor.applyAsInt(c.getValue())));
        columna.getStyleClass().add("columna-numerica");
        columna.setPrefWidth(ancho);
        columna.setSortable(false);
        return columna;
    }

    // Clic (o Enter) sobre una fila ejecuta la acción con el elemento de esa fila.
    static <T> void alElegirFila(TableView<T> tabla, Consumer<T> accion) {
        tabla.setRowFactory(t -> {
            TableRow<T> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (!fila.isEmpty() && evento.getButton() == MouseButton.PRIMARY) {
                    accion.accept(fila.getItem());
                }
            });
            return fila;
        });
        tabla.setOnKeyPressed(evento -> {
            T seleccionado = tabla.getSelectionModel().getSelectedItem();
            if (evento.getCode() == KeyCode.ENTER && seleccionado != null) {
                accion.accept(seleccionado);
            }
        });
    }
}