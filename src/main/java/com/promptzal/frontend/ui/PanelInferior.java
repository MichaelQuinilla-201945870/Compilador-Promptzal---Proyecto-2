package com.promptzal.frontend.ui;

import com.promptzal.backend.ResultadoCompilacion;
import com.promptzal.backend.errores.ErrorCompilador;
import com.promptzal.backend.lexico.TipoToken;
import com.promptzal.backend.lexico.Token;
import com.promptzal.backend.semantico.Simbolo;
import com.promptzal.frontend.tema.Iconos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import java.util.ArrayList;

// Panel de abajo: una pestaña por resultado de la compilacion
class PanelInferior extends StackPane {

    // Una fila de la tabla de tokens, el numero correlativo y el token
    private record FilaToken(int numero, Token token) { }

    private final TabPane pestanas = new TabPane();

    private final Label marcadorErrores = new Label("Compila el archivo con Shift+F10 para ver los errores.");
    private final Label marcadorTokens = new Label("Compila el archivo para ver sus tokens.");
    private final Label marcadorSimbolos = new Label("Compila el archivo para ver la tabla de símbolos.");
    private final Label marcadorAst = new Label("Compila el archivo para ver su árbol de sintaxis.");

    private final TableView<ErrorCompilador> tablaErrores = Tablas.crear(marcadorErrores);
    private final TableView<FilaToken> tablaTokens = Tablas.crear(marcadorTokens);
    private final TableView<Simbolo> tablaSimbolos = Tablas.crear(marcadorSimbolos);
    private final TreeView<ArbolAst.Nodo> arbolAst = new TreeView<>();

    private final Tab tabErrores;
    private final Tab tabTokens;
    private final Tab tabSimbolos;

    private BiConsumer<Integer, Integer> alIrA = (fila, columna) -> { };

    PanelInferior(Runnable alOcultar) {
        configurarTablaErrores();
        configurarTablaTokens();
        configurarTablaSimbolos();
        configurarArbol();

        tabErrores = pestana("Errores", Iconos.ERRORES, tablaErrores);
        tabTokens = pestana("Tokens", Iconos.TOKENS, tablaTokens);
        tabSimbolos = pestana("Símbolos", Iconos.SIMBOLOS, tablaSimbolos);
        Tab tabAst = pestana("AST", Iconos.AST, contenidoArbol());

        pestanas.getTabs().addAll(tabErrores, tabTokens, tabSimbolos, tabAst);
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Button ocultar = Componentes.botonIcono(Iconos.OCULTAR, "Ocultar el panel");
        ocultar.setOnAction(e -> alOcultar.run());
        StackPane.setAlignment(ocultar, Pos.TOP_RIGHT);
        StackPane.setMargin(ocultar, new Insets(3, 8, 0, 0));

        getChildren().addAll(pestanas, ocultar);
        setMinHeight(120);
    }

    // Lo que usa la ventana principal

    // se llama (fila, columna)
    void setAlIrA(BiConsumer<Integer, Integer> accion) {
        this.alIrA = accion;
    }

    void seleccionarErrores() {
        pestanas.getSelectionModel().select(tabErrores);
    }

    // Llena todas las pestañas con el resultado de una compilacion
    void mostrarResultado(ResultadoCompilacion r) {

        // Errores
        List<ErrorCompilador> errores = r.errores();
        tablaErrores.getItems().setAll(errores);
        tabErrores.setText(titulo("Errores", errores.size()));
        marcadorErrores.setText("La compilación terminó sin errores.");

        // Tokens (sin el EOF)
        List<FilaToken> filas = new ArrayList<>();
        for (Token token : r.tokens()) {
            if (token.getTipo() != TipoToken.EOF) {
                filas.add(new FilaToken(filas.size() + 1, token));
            }
        }
        tablaTokens.getItems().setAll(filas);
        tabTokens.setText(titulo("Tokens", filas.size()));
        marcadorTokens.setText("El archivo no tiene tokens.");

        // Simbolos y AST: solo existen si el archivo no hay errores sintacticos
        boolean hayAnalisis = r.tabla() != null && r.programa() != null;
        if (hayAnalisis) {
            List<Simbolo> simbolos = r.tabla().getSimbolos();
            tablaSimbolos.getItems().setAll(simbolos);
            tabSimbolos.setText(titulo("Símbolos", simbolos.size()));
            arbolAst.setRoot(ArbolAst.construir(r.programa()));
        } else {
            tablaSimbolos.getItems().clear();
            tabSimbolos.setText("Símbolos");
            marcadorSimbolos.setText("No hay tabla de símbolos: el archivo tiene errores sintácticos.");
            arbolAst.setRoot(null);
            marcadorAst.setText("No hay árbol: el archivo tiene errores sintácticos.");
        }
    }

    private static String titulo(String base, int cantidad) {
        return cantidad == 0 ? base : base + " " + cantidad;
    }

    // Errores
    private void configurarTablaErrores() {
        TableColumn<ErrorCompilador, String> tipo = Tablas.texto("Tipo", 130,
                e -> e.getTipo().getEtiqueta());
        tipo.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vacio) {
                super.updateItem(texto, vacio);
                if (vacio || texto == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                FontIcon icono = Iconos.crear(Iconos.ERRORES);
                icono.getStyleClass().add("icono-error");
                setGraphic(icono);
                setText(texto);
            }
        });
        TableColumn<ErrorCompilador, Number> linea = Tablas.numero("Línea", 70, ErrorCompilador::getFila);
        TableColumn<ErrorCompilador, Number> columna = Tablas.numero("Columna", 80, ErrorCompilador::getColumna);
        TableColumn<ErrorCompilador, String> descripcion = Tablas.texto("Descripción", 300,
                ErrorCompilador::getDescripcion);

        tablaErrores.getColumns().addAll(tipo, linea, columna, descripcion);
        Tablas.alElegirFila(tablaErrores, e -> alIrA.accept(e.getFila(), e.getColumna()));
    }

    // Tokens
    private void configurarTablaTokens() {
        TableColumn<FilaToken, Number> numero = Tablas.numero("#", 60, FilaToken::numero);
        TableColumn<FilaToken, Number> linea = Tablas.numero("Línea", 70, f -> f.token().getFila());
        TableColumn<FilaToken, Number> columna = Tablas.numero("Columna", 80, f -> f.token().getColumna());

        // El tipo se pinta con el color de su categoría, igual que en el editor
        TableColumn<FilaToken, FilaToken> tipo = new TableColumn<>("Tipo");
        tipo.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        tipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(FilaToken fila, boolean vacio) {
                super.updateItem(fila, vacio);
                getStyleClass().removeAll(CategoriaToken.TODAS);
                if (vacio || fila == null) {
                    setText(null);
                    return;
                }
                setText(fila.token().getTipo().name());
                String clase = CategoriaToken.de(fila.token().getTipo());
                if (clase != null) {
                    getStyleClass().add(clase);
                }
            }
        });
        tipo.setPrefWidth(140);
        tipo.setSortable(false);

        TableColumn<FilaToken, String> lexema = Tablas.texto("Lexema", 300, f -> f.token().getLexema());

        tablaTokens.getColumns().addAll(numero, linea, columna, tipo, lexema);
        Tablas.alElegirFila(tablaTokens, f -> alIrA.accept(f.token().getFila(), f.token().getColumna()));
    }

    // Simbolos
    private void configurarTablaSimbolos() {
        TableColumn<Simbolo, String> nombre = Tablas.texto("Nombre", 200, Simbolo::nombre);
        TableColumn<Simbolo, String> tipo = Tablas.texto("Tipo", 170, s -> s.tipo().getEtiqueta());
        TableColumn<Simbolo, String> ambito = Tablas.texto("Ámbito", 120, Simbolo::ambito);
        TableColumn<Simbolo, Number> linea = Tablas.numero("Línea", 70, Simbolo::fila);

        tablaSimbolos.getColumns().addAll(nombre, tipo, ambito, linea);
        tablaSimbolos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Tablas.alElegirFila(tablaSimbolos, s -> alIrA.accept(s.fila(), s.columna()));
    }

    // AST
    private void configurarArbol() {
        arbolAst.setCellFactory(arbol -> new TreeCell<>() {
            @Override
            protected void updateItem(ArbolAst.Nodo nodo, boolean vacio) {
                super.updateItem(nodo, vacio);
                getStyleClass().removeAll(CategoriaToken.TODAS);
                if (vacio || nodo == null) {
                    setText(null);
                    return;
                }
                setText(nodo.texto());
                if (nodo.clase() != null) {
                    getStyleClass().add(nodo.clase());
                }
            }
        });
        arbolAst.setOnMouseClicked(evento -> irADeNodoSeleccionado());
        arbolAst.setOnKeyPressed(evento -> {
            if (evento.getCode() == javafx.scene.input.KeyCode.ENTER) {
                irADeNodoSeleccionado();
            }
        });
    }

    private void irADeNodoSeleccionado() {
        TreeItem<ArbolAst.Nodo> seleccionado = arbolAst.getSelectionModel().getSelectedItem();
        if (seleccionado != null && seleccionado.getValue().fila() > 0) {
            alIrA.accept(seleccionado.getValue().fila(), seleccionado.getValue().columna());
        }
    }

    private StackPane contenidoArbol() {
        marcadorAst.getStyleClass().add("estado-vacio");
        marcadorAst.setWrapText(true);
        marcadorAst.visibleProperty().bind(arbolAst.rootProperty().isNull());
        arbolAst.visibleProperty().bind(arbolAst.rootProperty().isNotNull());
        return new StackPane(arbolAst, marcadorAst);
    }

    private static Tab pestana(String titulo, String icono, javafx.scene.Node contenido) {
        Tab tab = new Tab(titulo, contenido);
        tab.setGraphic(Iconos.crear(icono));
        tab.setClosable(false);
        return tab;
    }
}