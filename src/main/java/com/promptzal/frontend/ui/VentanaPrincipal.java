package com.promptzal.frontend.ui;

import com.promptzal.backend.Compilador;
import com.promptzal.backend.ResultadoCompilacion;
import com.promptzal.backend.errores.ErrorCompilador;
import com.promptzal.backend.errores.TipoError;

import com.promptzal.frontend.tema.Iconos;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import java.util.List;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import java.util.HashSet;
import java.util.Set;

// Ventana principal: menu arriba, tiras laterales, paneles con divisores y barra de estado.
public class VentanaPrincipal {

    private static final double ANCHO_PROYECTO = 240;
    private static final double ANCHO_PROMPTZAL = 380;
    private static final double ALTO_INFERIOR = 240;

    private final BooleanProperty proyectoVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty promptzalVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty inferiorVisible = new SimpleBooleanProperty(true);

    private final BorderPane raiz = new BorderPane();
    private final SplitPane horizontal = new SplitPane();   // proyecto | centro | promptzal
    private final SplitPane vertical = new SplitPane();     // editor / panel inferior

    private final PanelProyecto panelProyecto = new PanelProyecto(() -> proyectoVisible.set(false));
    private final AreaEditor areaEditor = new AreaEditor();
    private final PanelInferior panelInferior = new PanelInferior(() -> inferiorVisible.set(false));
    private final PanelPromptzal panelPromptzal = new PanelPromptzal(() -> promptzalVisible.set(false));
    private final BarraEstado barraEstado = new BarraEstado();

    private boolean anchoInicialAplicado = false;
    private boolean altoInicialAplicado = false;

    private final Compilador compilador = new Compilador();
    private DocumentoPz documentoCompilado;      // archivo al que pertenecen los errores mostrados

    public VentanaPrincipal() {
        vertical.setOrientation(Orientation.VERTICAL);

        // Al cambiar el tamaño de la ventana, los paneles laterales conservan su tamaño
        SplitPane.setResizableWithParent(panelProyecto, false);
        SplitPane.setResizableWithParent(panelPromptzal, false);
        SplitPane.setResizableWithParent(panelInferior, false);

        areaEditor.setAlInformar(barraEstado::setMensaje);
        panelInferior.setAlIrA(this::irA);
        panelPromptzal.setAlInformar(barraEstado::setMensaje);


        raiz.setTop(new VBox(
                new BarraMenu(proyectoVisible, promptzalVisible, inferiorVisible, areaEditor, this::compilar, this::salir),
                new BarraHerramientas(areaEditor, this::compilar)));

        raiz.setLeft(crearBarraIzquierda());
        raiz.setRight(crearBarraDerecha());
        raiz.setCenter(horizontal);
        raiz.setBottom(barraEstado);

        proyectoVisible.addListener((obs, antes, ahora) -> reorganizar());
        promptzalVisible.addListener((obs, antes, ahora) -> reorganizar());
        inferiorVisible.addListener((obs, antes, ahora) -> reorganizar());
        reorganizar();

        horizontal.widthProperty().addListener((obs, antes, ancho) -> {
            if (!anchoInicialAplicado && ancho.doubleValue() > 0) {
                anchoInicialAplicado = true;
                aplicarProporcionHorizontal();
            }
        });
        vertical.heightProperty().addListener((obs, antes, alto) -> {
            if (!altoInicialAplicado && alto.doubleValue() > 0) {
                altoInicialAplicado = true;
                aplicarProporcionVertical();
            }
        });
    }

    public Parent getRaiz() {
        return raiz;
    }

    public void alCerrarVentana(WindowEvent evento) {
        if (!areaEditor.puedeCerrarTodo()) {
            evento.consume();                    // el usuario cancelo: la ventana sigue abierta
        }
    }

    private void salir() {
        Window ventana = raiz.getScene().getWindow();
        ventana.fireEvent(new WindowEvent(ventana, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    // Compila el archivo de la pestaña activa
    private void compilar() {
        DocumentoPz documento = areaEditor.activo();
        if (documento == null) {
            return;
        }

        ResultadoCompilacion resultado;
        try {
            resultado = compilador.compilar(documento.getArea().getText());
        } catch (RuntimeException e) {                       // un fallo inesperado no debe tumbar la interfaz
            e.printStackTrace();
            Dialogos.error(raiz.getScene().getWindow(),
                    "La compilación falló de forma inesperada", String.valueOf(e));
            return;
        }
        documentoCompilado = documento;
        documento.marcarErrores(posicionesConError(resultado.errores()));

        // Errores, tokens, símbolos y AST
        panelInferior.mostrarResultado(resultado);
        List<ErrorCompilador> errores = resultado.errores();
        if (!errores.isEmpty()) {
            inferiorVisible.set(true);                       // si estaba oculto, se muestra
            panelInferior.seleccionarErrores();
        }

        // Promptzal: se genera aunque haya errores semanticos, pero no con errores sintacticos
        if (resultado.promptzal() != null) {
            panelPromptzal.mostrar(resultado.promptzal(), documento.getNombre());
            promptzalVisible.set(true);
        } else {
            panelPromptzal.mostrarSinPrompt(
                    "No se generó el prompt porque el archivo tiene errores sintácticos. "
                            + "Corrígelos y vuelve a compilar.");
        }

        barraEstado.setMensaje(resumen(errores.size()));
    }

    private static String resumen(int errores) {
        if (errores == 0) {
            return "Compilación correcta";
        }
        return "Compilación terminada con " + errores + (errores == 1 ? " error" : " errores");
    }

    // Posiciones "fila:columna" de los errores sintacticos y semanticos, para subrayar su token
    private static Set<String> posicionesConError(List<ErrorCompilador> errores) {
        Set<String> posiciones = new HashSet<>();
        for (ErrorCompilador error : errores) {
            if (error.getTipo() != TipoError.LEXICO) {       // los léxicos ya se ven en vivo
                posiciones.add(error.getFila() + ":" + error.getColumna());
            }
        }
        return posiciones;
    }

    private void irA(int fila, int columna) {
        if (documentoCompilado != null) {
            areaEditor.irA(documentoCompilado, fila, columna);
        }
    }

    // Tiras laterales
    private Node crearBarraIzquierda() {
        BarraLateral barra = new BarraLateral(true);
        barra.arriba(Iconos.PROYECTO, "Proyecto (Alt+1)", proyectoVisible);
        barra.abajo(Iconos.ERRORES, "Errores y resultados (Alt+3)", inferiorVisible);
        return barra;
    }

    private Node crearBarraDerecha() {
        BarraLateral barra = new BarraLateral(false);
        barra.arriba(Iconos.PROMPTZAL, "Promptzal (Alt+2)", promptzalVisible);
        return barra;
    }

    // Mostrar y ocultar paneles
    private void reorganizar() {
        vertical.getItems().clear();
        vertical.getItems().add(areaEditor);
        if (inferiorVisible.get()) {
            vertical.getItems().add(panelInferior);
        }

        horizontal.getItems().clear();
        if (proyectoVisible.get()) {
            horizontal.getItems().add(panelProyecto);
        }
        horizontal.getItems().add(vertical);
        if (promptzalVisible.get()) {
            horizontal.getItems().add(panelPromptzal);
        }

        Platform.runLater(() -> {
            aplicarProporcionHorizontal();
            aplicarProporcionVertical();
        });
    }

    private double anchoFijo(Node nodo) {
        if (nodo == panelProyecto) {
            return ANCHO_PROYECTO;
        }
        if (nodo == panelPromptzal) {
            return ANCHO_PROMPTZAL;
        }
        return 0;       // el centro ocupa lo que sobra
    }

    private void aplicarProporcionHorizontal() {
        double ancho = horizontal.getWidth();
        List<Node> items = horizontal.getItems();
        if (ancho <= 0 || items.size() < 2) {
            return;
        }
        double fijos = 0;
        for (Node nodo : items) {
            fijos += anchoFijo(nodo);
        }
        double[] posiciones = new double[items.size() - 1];
        double acumulado = 0;
        for (int i = 0; i < posiciones.length; i++) {
            Node nodo = items.get(i);
            acumulado += (nodo == vertical) ? ancho - fijos : anchoFijo(nodo);
            posiciones[i] = acumulado / ancho;
        }
        horizontal.setDividerPositions(posiciones);
    }

    private void aplicarProporcionVertical() {
        double alto = vertical.getHeight();
        if (alto <= 0 || vertical.getItems().size() < 2) {
            return;
        }
        vertical.setDividerPositions((alto - ALTO_INFERIOR) / alto);
    }
}