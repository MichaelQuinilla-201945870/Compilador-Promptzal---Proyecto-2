package com.promptzal.frontend.ui;

import com.promptzal.frontend.tema.Iconos;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

// Panel de la derecha: el promptzal generado, para copiar o guardar

class PanelPromptzal extends BorderPane {

    private final CodeArea area = new CodeArea();
    private final Label textoVacio = new Label("Compila el archivo y el prompt generado aparecerá aquí.");
    private final BooleanProperty hayPrompt = new SimpleBooleanProperty(false);

    private String prompt;                         // null si no hay
    private String nombreBase = "promptzal";
    private File ultimaCarpeta;
    private Consumer<String> alInformar = mensaje -> { };

    PanelPromptzal(Runnable alOcultar) {
        Button copiar = Componentes.botonIcono(Iconos.COPIAR, "Copiar el prompt");
        copiar.setOnAction(e -> copiarAlPortapapeles());
        copiar.disableProperty().bind(hayPrompt.not());

        Button guardar = Componentes.botonIcono(Iconos.GUARDAR, "Guardar como…");
        guardar.setOnAction(e -> guardarComo());
        guardar.disableProperty().bind(hayPrompt.not());

        Button ocultar = Componentes.botonIcono(Iconos.OCULTAR, "Ocultar el panel");
        ocultar.setOnAction(e -> alOcultar.run());

        area.setEditable(false);                   // se puede seleccionar y copiar, pero no editar
        area.setWrapText(true);
        VirtualizedScrollPane<CodeArea> desplazamiento = new VirtualizedScrollPane<>(area);
        desplazamiento.visibleProperty().bind(hayPrompt);

        textoVacio.getStyleClass().add("estado-vacio");
        textoVacio.setWrapText(true);
        textoVacio.visibleProperty().bind(hayPrompt.not());
        StackPane vacio = new StackPane(textoVacio);
        vacio.setPadding(new Insets(16));

        setTop(Componentes.encabezado("Promptzal", copiar, guardar, ocultar));
        setCenter(new StackPane(vacio, desplazamiento));
        getStyleClass().add("panel-promptzal");
        setMinWidth(240);
    }

    // Mensajes para la barra de estado ("Prompt copiado")
    void setAlInformar(Consumer<String> alInformar) {
        this.alInformar = alInformar;
    }

    // Muestra el prompt generado
    void mostrar(String prompt, String archivoOrigen) {
        this.prompt = prompt;
        this.nombreBase = archivoOrigen.endsWith(".pz")
                ? archivoOrigen.substring(0, archivoOrigen.length() - 3)
                : archivoOrigen;

        area.replaceText(prompt);
        for (int i = 0; i < area.getParagraphs().size(); i++) {
            if (area.getParagraph(i).getText().startsWith("#")) {
                area.setStyle(i, List.of("pz-md-titulo"));      // títulos de Markdown
            }
        }
        area.moveTo(0);
        area.scrollYToPixel(0);
        hayPrompt.set(true);
    }

    // No hay prompt (por ejemplo, por errores sintacticos)
    void mostrarSinPrompt(String motivo) {
        prompt = null;
        area.clear();
        textoVacio.setText(motivo);
        hayPrompt.set(false);
    }

    private void copiarAlPortapapeles() {
        ClipboardContent contenido = new ClipboardContent();
        contenido.putString(prompt);
        Clipboard.getSystemClipboard().setContent(contenido);
        alInformar.accept("Prompt copiado al portapapeles");
    }

    private void guardarComo() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar promptzal");
        FileChooser.ExtensionFilter markdown = new FileChooser.ExtensionFilter("Markdown (*.md)", "*.md");
        FileChooser.ExtensionFilter texto = new FileChooser.ExtensionFilter("Texto (*.txt)", "*.txt");
        selector.getExtensionFilters().addAll(markdown, texto);
        selector.setInitialFileName(nombreBase + ".md");
        File carpeta = carpetaInicial();
        if (carpeta != null) {
            selector.setInitialDirectory(carpeta);
        }

        File archivo = selector.showSaveDialog(getScene().getWindow());
        if (archivo == null) {
            return;                                 // canceló
        }
        String extensionPorDefecto = selector.getSelectedExtensionFilter() == texto ? ".txt" : ".md";
        Path destino = conExtension(archivo.toPath(), extensionPorDefecto);

        try {
            Files.writeString(destino, prompt);     // UTF-8
            if (destino.getParent() != null) {
                ultimaCarpeta = destino.getParent().toFile();
            }
            alInformar.accept("Promptzal guardado en " + destino.getFileName());
        } catch (IOException e) {
            Dialogos.error(getScene().getWindow(),
                    "No se pudo guardar " + destino.getFileName(), e.getMessage());
        }
    }

    // ultima carpeta usada; si no hay, ejemplos/salidas del proyecto; si no, la del proyecto
    private File carpetaInicial() {
        if (ultimaCarpeta != null && ultimaCarpeta.isDirectory()) {
            return ultimaCarpeta;
        }
        File proyecto = new File(System.getProperty("user.dir"));
        File salidas = new File(proyecto, "ejemplos/salidas");
        if (salidas.isDirectory()) {
            return salidas;
        }
        File ejemplos = new File(proyecto, "ejemplos");
        if (ejemplos.isDirectory()) {
            return ejemplos;
        }
        return proyecto.isDirectory() ? proyecto : null;
    }

    private static Path conExtension(Path ruta, String extensionPorDefecto) {
        String nombre = ruta.getFileName().toString().toLowerCase();
        if (nombre.endsWith(".md") || nombre.endsWith(".txt")) {
            return ruta;
        }
        return ruta.resolveSibling(ruta.getFileName() + extensionPorDefecto);
    }
}