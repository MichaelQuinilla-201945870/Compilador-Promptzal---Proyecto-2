package com.promptzal.frontend.ui;

import com.promptzal.frontend.tema.Fuentes;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;

//: ventana de prueba para comprobar tema, fuentes, editor e íconos.
public class VentanaPrincipal {

    private static final String MUESTRA = """
            @modelo "claude-sonnet-4-6"
            // Agente de ejemplo
            AGENTE analista {
              contexto = "Eres un analista de datos experto"
              PREGUNTAR "Cuales son las tendencias?" SOBRE ventas -> tendencias
              RESUMIR tendencias EN 100 palabras -> resumen
            }
            """;

    private final BorderPane raiz = new BorderPane();

    public VentanaPrincipal() {
        raiz.setTop(crearBarraHerramientas());
        raiz.setCenter(crearEditor());
        raiz.setBottom(crearBarraEstado());
    }

    public Parent getRaiz() {
        return raiz;
    }

    private Node crearBarraHerramientas() {
        Label proyecto = new Label("promptzal");
        proyecto.getStyleClass().add("nombre-proyecto");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Button compilar = new Button("Compilar", new FontIcon("mdi2p-play"));
        compilar.getStyleClass().add("boton-compilar");

        HBox barra = new HBox(8, proyecto, espacio, compilar);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("barra-herramientas");
        return barra;
    }

    private Node crearEditor() {
        CodeArea area = new CodeArea(MUESTRA);
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));

        // Solo para la vista previa
        pintar(area, "@modelo", "pz-directiva");
        pintar(area, "\"claude-sonnet-4-6\"", "pz-cadena");
        pintar(area, "// Agente de ejemplo", "pz-comentario");
        pintar(area, "AGENTE", "pz-reservada");
        pintar(area, "contexto", "pz-reservada");
        pintar(area, "PREGUNTAR", "pz-comando");
        pintar(area, "RESUMIR", "pz-comando");
        pintar(area, "SOBRE", "pz-conector");
        pintar(area, "->", "pz-conector");
        pintar(area, "100", "pz-numero");

        return new VirtualizedScrollPane<>(area);
    }

    private static void pintar(CodeArea area, String fragmento, String clase) {
        int inicio = area.getText().indexOf(fragmento);
        if (inicio >= 0) {
            area.setStyle(inicio, inicio + fragmento.length(), List.of(clase));
        }
    }

    private Node crearBarraEstado() {
        Label mensaje = new Label("Vista previa de la interfaz");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label javafx = new Label("JavaFX " + System.getProperty("javafx.runtime.version"));
        Label java = new Label("Java " + System.getProperty("java.version"));
        Label mono = new Label("JetBrains Mono: " + estado(Fuentes.disponible("JetBrains Mono")));
        Label inter = new Label("Inter: " + estado(Fuentes.disponible("Inter")));
        for (Label l : List.of(javafx, java, mono, inter)) {
            l.getStyleClass().add("texto-silenciado");
        }

        HBox barra = new HBox(16, mensaje, espacio, mono, inter, java, javafx);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("barra-estado");
        return barra;
    }

    private static String estado(boolean cargada) {
        return cargada ? "cargada" : "no encontrada";
    }
}