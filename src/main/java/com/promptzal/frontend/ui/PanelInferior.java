package com.promptzal.frontend.ui;

import com.promptzal.backend.errores.ErrorCompilador;
import com.promptzal.frontend.tema.Iconos;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;
import java.util.function.Consumer;

// Panel de abajo: una pestaña por resultado de la compilacion
class PanelInferior extends StackPane {

    private final TabPane pestanas = new TabPane();
    private final TableView<ErrorCompilador> tablaErrores = new TableView<>();
    private final Label textoVacioErrores =
            new Label("Compila el archivo con Shift+F10 para ver los errores.");
    private final Tab tabErrores;
    private Consumer<ErrorCompilador> alSeleccionarError = error -> { };

    PanelInferior(Runnable alOcultar) {
        configurarTablaErrores();
        tabErrores = pestana("Errores", Iconos.ERRORES, tablaErrores);

        pestanas.getTabs().addAll(
                tabErrores,
                pestana("Tokens", Iconos.TOKENS,
                        Componentes.estadoVacio("Compila el archivo para ver sus tokens.")),
                pestana("Símbolos", Iconos.SIMBOLOS,
                        Componentes.estadoVacio("Compila el archivo para ver la tabla de símbolos.")),
                pestana("AST", Iconos.AST,
                        Componentes.estadoVacio("Compila el archivo para ver su árbol de sintaxis.")));
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Button ocultar = Componentes.botonIcono(Iconos.OCULTAR, "Ocultar el panel");
        ocultar.setOnAction(e -> alOcultar.run());
        StackPane.setAlignment(ocultar, Pos.TOP_RIGHT);
        StackPane.setMargin(ocultar, new Insets(3, 8, 0, 0));

        getChildren().addAll(pestanas, ocultar);
        setMinHeight(120);
    }

    // Lo que usa la ventana principal

    // Llena la tabla con los errores de la ultima compilación (lista vacía = sin errores)
    void mostrarErrores(List<ErrorCompilador> errores) {
        tablaErrores.getItems().setAll(errores);
        tabErrores.setText(errores.isEmpty() ? "Errores" : "Errores " + errores.size());
        textoVacioErrores.setText("La compilación terminó sin errores.");
    }

    void seleccionarErrores() {
        pestanas.getSelectionModel().select(tabErrores);
    }

    //Se llama al hacer clic (o Enter) sobre un error de la tabla
    void setAlSeleccionarError(Consumer<ErrorCompilador> accion) {
        this.alSeleccionarError = accion;
    }

    // Tabla de errores
    private void configurarTablaErrores() {
        TableColumn<ErrorCompilador, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().getEtiqueta()));
        colTipo.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String tipo, boolean vacio) {
                super.updateItem(tipo, vacio);
                if (vacio || tipo == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                FontIcon icono = Iconos.crear(Iconos.ERRORES);
                icono.getStyleClass().add("icono-error");
                setGraphic(icono);
                setText(tipo);
            }
        });
        colTipo.setPrefWidth(130);
        colTipo.setSortable(false);

        TableColumn<ErrorCompilador, Number> colLinea = new TableColumn<>("Línea");
        colLinea.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getFila()));
        colLinea.getStyleClass().add("columna-numerica");
        colLinea.setPrefWidth(70);
        colLinea.setSortable(false);

        TableColumn<ErrorCompilador, Number> colColumna = new TableColumn<>("Columna");
        colColumna.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getColumna()));
        colColumna.getStyleClass().add("columna-numerica");
        colColumna.setPrefWidth(80);
        colColumna.setSortable(false);

        TableColumn<ErrorCompilador, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));
        colDescripcion.setSortable(false);

        tablaErrores.getColumns().addAll(colTipo, colLinea, colColumna, colDescripcion);
        tablaErrores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        textoVacioErrores.getStyleClass().add("estado-vacio");
        tablaErrores.setPlaceholder(textoVacioErrores);

        // Clic en una fila y se salta a la posicion del error
        tablaErrores.setRowFactory(tabla -> {
            TableRow<ErrorCompilador> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (!fila.isEmpty() && evento.getButton() == MouseButton.PRIMARY) {
                    alSeleccionarError.accept(fila.getItem());
                }
            });
            return fila;
        });
        // Enter sobre la fila seleccionada hace lo mismo (para usar el teclado)
        tablaErrores.setOnKeyPressed(evento -> {
            ErrorCompilador seleccionado = tablaErrores.getSelectionModel().getSelectedItem();
            if (evento.getCode() == KeyCode.ENTER && seleccionado != null) {
                alSeleccionarError.accept(seleccionado);
            }
        });
    }

    private static Tab pestana(String titulo, String icono, Node contenido) {
        Tab tab = new Tab(titulo, contenido);
        tab.setGraphic(Iconos.crear(icono));
        tab.setClosable(false);
        return tab;
    }
}