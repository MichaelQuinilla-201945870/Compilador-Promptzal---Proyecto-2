package com.promptzal.frontend.ui;

import com.promptzal.backend.Compilador;
import com.promptzal.frontend.tema.Iconos;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import org.fxmisc.richtext.util.UndoUtils;

// Un archivo abierto en el editor: su texto, su pestaña y si tiene cambios sin guardar (ahora tiene color)
final class DocumentoPz {

    private static final Compilador COMPILADOR = new Compilador();

    private final CodeArea area = new CodeArea();
    private final Tab pestana = new Tab();
    private Path ruta;                 // null mientras el archivo no se haya guardado
    private String nombreTemporal;     // "sin-titulo1.pz" mientras no tenga ruta
    private String textoGuardado;      // el texto tal como está en el disco
    private Set<String> marcas = Set.of();   // tokens con error de la ultima compilacion

    private DocumentoPz(Path ruta, String nombreTemporal, String texto) {
        this.ruta = ruta;
        this.nombreTemporal = nombreTemporal;
        this.textoGuardado = texto;

        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.setUndoManager(UndoUtils.plainTextUndoManager(area));   // los colores no cuentan como cambios
        area.replaceText(texto);
        area.moveTo(0);
        area.getUndoManager().forgetHistory();      // deshacer no debe vaciar el archivo recien abierto
        area.textProperty().addListener((obs, antes, ahora) -> actualizarTitulo());

        // Los colores se recalculan poco despues de que el usuario deja de escribir
        area.multiPlainChanges().successionEnds(Duration.ofMillis(120)).subscribe(cambios -> {
            marcas = Set.of();                      // las marcas ya no coinciden con el texto
            resaltar();
        });
        resaltar();

        FontIcon icono = Iconos.crear(Iconos.ARCHIVO_PZ);
        icono.getStyleClass().add("icono-pz");
        pestana.setGraphic(icono);
        pestana.setContent(new VirtualizedScrollPane<>(area));
        pestana.setUserData(this);                  // desde la pestaña se llega al documento
        actualizarTitulo();
        actualizarTooltip();
    }

    // Documento en blanco, todavia sin archivo en disco
    static DocumentoPz nuevo(String nombreTemporal) {
        return new DocumentoPz(null, nombreTemporal, "");
    }

    // Lee un archivo UTF-8. Lanza excepcion si no existe o no es UTF-8 valido.
    static DocumentoPz abrir(Path ruta) throws IOException {
        String texto = normalizarSaltos(Files.readString(ruta));
        return new DocumentoPz(ruta.toAbsolutePath().normalize(), null, texto);
    }

    void guardarEn(Path destino) throws IOException {
        Path absoluto = destino.toAbsolutePath().normalize();
        String texto = area.getText();
        Files.writeString(absoluto, texto);          // UTF-8
        ruta = absoluto;
        nombreTemporal = null;
        textoGuardado = texto;
        actualizarTitulo();
        actualizarTooltip();
    }

    // Subraya los tokens en estas posiciones ("fila:columna") hasta que el texto cambie
    void marcarErrores(Set<String> posiciones) {
        marcas = posiciones;
        resaltar();
    }

    // Recalcula los colores con el lexer del compilador
    void resaltar() {
        String texto = area.getText();
        if (texto.isEmpty()) {
            return;
        }
        area.setStyleSpans(0, Resaltador.calcular(texto, COMPILADOR.tokenizar(texto), marcas));
    }

    boolean estaModificado() {
        return !area.getText().equals(textoGuardado);
    }

    boolean esNuevo() {
        return ruta == null;
    }

    Path getRuta() {
        return ruta;
    }

    String getNombre() {
        return ruta != null ? ruta.getFileName().toString() : nombreTemporal;
    }

    CodeArea getArea() {
        return area;
    }

    Tab getPestana() {
        return pestana;
    }

    private void actualizarTitulo() {
        pestana.setText(getNombre() + (estaModificado() ? " •" : ""));
    }

    private void actualizarTooltip() {
        pestana.setTooltip(ruta == null ? null : new Tooltip(ruta.toString()));
    }

    // El editor trabaja con \n; los archivos de Windows (\r\n) se convierten al abrirlos.
    private static String normalizarSaltos(String texto) {
        return texto.replace("\r\n", "\n").replace('\r', '\n');
    }
}