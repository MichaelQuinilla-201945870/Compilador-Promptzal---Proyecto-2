package com.promptzal.frontend.ui;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.fxmisc.richtext.CodeArea;

// Zona central: ahora es pestañas de archivos .pz y operaciones de abrir, guardar y cerrar lista
class AreaEditor extends StackPane {

    private final TabPane pestanas = new TabPane();
    private final BooleanProperty hayDocumento = new SimpleBooleanProperty(false);

    private BiConsumer<Integer, Integer> alMoverCursor = (linea, columna) -> { };
    private Consumer<String> alInformar = mensaje -> { };
    private File ultimoDirectorio;
    private int contadorSinTitulo = 0;

    AreaEditor() {
        Node vacio = Componentes.estadoVacio("Abre un archivo .pz con Ctrl+O o crea uno nuevo con Ctrl+N.");
        vacio.visibleProperty().bind(hayDocumento.not());
        pestanas.visibleProperty().bind(hayDocumento);
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        getStyleClass().add("area-editor");
        getChildren().addAll(vacio, pestanas);

        pestanas.getTabs().addListener((ListChangeListener<Tab>) cambio ->
                hayDocumento.set(!pestanas.getTabs().isEmpty()));
        pestanas.getSelectionModel().selectedItemProperty().addListener(
                (obs, antes, ahora) -> avisarPosicion());
    }

    // Conexión con el resto de la ventana
    ReadOnlyBooleanProperty hayDocumentoProperty() {
        return hayDocumento;
    }

    // Avisa (línea, columna) del cursor del archivo activo; (0, 0) si no hay ninguno.
    void setAlMoverCursor(BiConsumer<Integer, Integer> alMover) {
        this.alMoverCursor = alMover;
        avisarPosicion();
    }

    // Mensajes para la barra de estado ("Guardado archivo.pz").
    void setAlInformar(Consumer<String> alInformar) {
        this.alInformar = alInformar;
    }

    // Documento de la pestaña seleccionada, o null si no hay ninguno.
    DocumentoPz activo() {
        Tab tab = pestanas.getSelectionModel().getSelectedItem();
        return tab == null ? null : (DocumentoPz) tab.getUserData();
    }

    // Archivo
    void nuevo() {
        contadorSinTitulo++;
        agregar(DocumentoPz.nuevo("sin-titulo" + contadorSinTitulo + ".pz"));
    }

    void abrir() {
        FileChooser selector = selector("Abrir archivo");
        File archivo = selector.showOpenDialog(ventana());
        if (archivo != null) {
            abrirRuta(archivo.toPath());
        }
    }

    void abrirRuta(Path ruta) {
        Path absoluta = ruta.toAbsolutePath().normalize();

        for (DocumentoPz d : documentos()) {                // si ya está abierto, solo se selecciona
            if (absoluta.equals(d.getRuta())) {
                pestanas.getSelectionModel().select(d.getPestana());
                return;
            }
        }
        try {
            agregar(DocumentoPz.abrir(absoluta));
            recordarDirectorio(absoluta);
            alInformar.accept("Abierto " + absoluta.getFileName());
        } catch (CharacterCodingException e) {
            Dialogos.error(ventana(), "No se pudo abrir " + absoluta.getFileName(),
                    "El archivo no está codificado en UTF-8.");
        } catch (IOException e) {
            Dialogos.error(ventana(), "No se pudo abrir " + absoluta.getFileName(), e.getMessage());
        }
    }

    boolean guardar() {
        DocumentoPz d = activo();
        return d != null && guardarDocumento(d);
    }

    boolean guardarComo() {
        DocumentoPz d = activo();
        return d != null && guardarDocumentoComo(d);
    }

    void cerrarActiva() {
        DocumentoPz d = activo();
        if (d != null && confirmarCierre(d)) {
            pestanas.getTabs().remove(d.getPestana());
        }
    }

    // Pregunta por cada archivo con cambios. false si el usuario lo cancelo
    boolean puedeCerrarTodo() {
        for (DocumentoPz d : documentos()) {
            if (!confirmarCierre(d)) {
                return false;
            }
        }
        return true;
    }

    // Edicion (en el menu Editar de la barra de arriba)
    void deshacer() {
        DocumentoPz d = activo();
        if (d != null) {
            d.getArea().undo();
        }
    }

    void rehacer() {
        DocumentoPz d = activo();
        if (d != null) {
            d.getArea().redo();
        }
    }

    void cortar() {
        DocumentoPz d = activo();
        if (d != null) {
            d.getArea().cut();
        }
    }

    void copiar() {
        DocumentoPz d = activo();
        if (d != null) {
            d.getArea().copy();
        }
    }

    void pegar() {
        DocumentoPz d = activo();
        if (d != null) {
            d.getArea().paste();
        }
    }

    // Lleva el cursor a (fila, columna), contando desde 1
    void irA(DocumentoPz documento, int fila, int columna) {
        if (!pestanas.getTabs().contains(documento.getPestana())) {
            return;                                    // el archivo ya se cerró
        }
        pestanas.getSelectionModel().select(documento.getPestana());

        CodeArea area = documento.getArea();
        // Si el archivo cambio desde la compilacion, la posicion se ajusta al texto actual
        int parrafo = Math.max(0, Math.min(fila - 1, area.getParagraphs().size() - 1));
        int posicion = Math.max(0, Math.min(columna - 1, area.getParagraphLength(parrafo)));
        area.moveTo(parrafo, posicion);

        Platform.runLater(() -> {
            area.requestFollowCaret();                 // para hacer visible el cursor si estaba fuera de pantalla
            area.requestFocus();
        });
    }

    // Internos
    private void agregar(DocumentoPz documento) {
        Tab tab = documento.getPestana();
        tab.setClosable(true);
        tab.setOnCloseRequest(evento -> {                  // la X de la pestaña
            if (!confirmarCierre(documento)) {
                evento.consume();
            }
        });
        documento.getArea().caretPositionProperty().addListener((obs, antes, ahora) -> {
            if (activo() == documento) {
                avisarPosicion();
            }
        });

        pestanas.getTabs().add(tab);
        pestanas.getSelectionModel().select(tab);
        Platform.runLater(() -> documento.getArea().requestFocus());
    }

    private boolean guardarDocumento(DocumentoPz documento) {
        if (documento.esNuevo()) {
            return guardarDocumentoComo(documento);
        }
        return escribir(documento, documento.getRuta());
    }

    private boolean guardarDocumentoComo(DocumentoPz documento) {
        FileChooser selector = selector("Guardar como");
        selector.setInitialFileName(documento.getNombre());
        if (documento.getRuta() != null && documento.getRuta().getParent() != null) {
            selector.setInitialDirectory(documento.getRuta().getParent().toFile());
        }
        File archivo = selector.showSaveDialog(ventana());
        if (archivo == null) {
            return false;                                   // cancelo
        }
        return escribir(documento, conExtension(archivo.toPath()));
    }

    private boolean escribir(DocumentoPz documento, Path destino) {
        try {
            documento.guardarEn(destino);
            recordarDirectorio(destino);
            alInformar.accept("Guardado " + documento.getNombre());
            return true;
        } catch (IOException e) {
            Dialogos.error(ventana(), "No se pudo guardar " + destino.getFileName(), e.getMessage());
            return false;
        }
    }

    // Si hay cambios sin guardar, pregunta que hacer. false significa "no cierres"
    private boolean confirmarCierre(DocumentoPz documento) {
        if (!documento.estaModificado()) {
            return true;
        }
        pestanas.getSelectionModel().select(documento.getPestana());   // que se vea de qué archivo se habla
        return switch (Dialogos.confirmarCambios(ventana(), documento.getNombre())) {
            case GUARDAR -> guardarDocumento(documento);
            case DESCARTAR -> true;
            case CANCELAR -> false;
        };
    }

    private void avisarPosicion() {
        DocumentoPz d = activo();
        if (d == null) {
            alMoverCursor.accept(0, 0);
        } else {
            alMoverCursor.accept(d.getArea().getCurrentParagraph() + 1, d.getArea().getCaretColumn() + 1);
        }
    }

    private List<DocumentoPz> documentos() {
        List<DocumentoPz> lista = new ArrayList<>();
        for (Tab tab : pestanas.getTabs()) {
            lista.add((DocumentoPz) tab.getUserData());
        }
        return lista;
    }

    private Window ventana() {
        return getScene() == null ? null : getScene().getWindow();
    }

    private FileChooser selector(String titulo) {
        FileChooser selector = new FileChooser();
        selector.setTitle(titulo);
        selector.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos PromptZal (*.pz)", "*.pz"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        File inicial = directorioInicial();
        if (inicial != null) {
            selector.setInitialDirectory(inicial);
        }
        return selector;
    }

    // ultimo directorio usado.
    private File directorioInicial() {
        if (ultimoDirectorio != null && ultimoDirectorio.isDirectory()) {
            return ultimoDirectorio;
        }
        File proyecto = new File(System.getProperty("user.dir"));
        File ejemplos = new File(proyecto, "ejemplos");
        if (ejemplos.isDirectory()) {
            return ejemplos;
        }
        return proyecto.isDirectory() ? proyecto : null;
    }

    private void recordarDirectorio(Path archivo) {
        if (archivo.getParent() != null) {
            ultimoDirectorio = archivo.getParent().toFile();
        }
    }

    // Agrega .pz por defecto
    private static Path conExtension(Path ruta) {
        String nombre = ruta.getFileName().toString();
        return nombre.toLowerCase().endsWith(".pz") ? ruta : ruta.resolveSibling(nombre + ".pz");
    }
}