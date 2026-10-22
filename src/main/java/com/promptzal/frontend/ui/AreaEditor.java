package com.promptzal.frontend.ui;

import com.promptzal.frontend.tema.Iconos;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;
import java.util.function.BiConsumer;

// Zona central: pestañas de archivos con su editor */
class AreaEditor extends TabPane {

    //Muestra de para ver que sirva
    private static final String MUESTRA = """ 
            @modelo "claude-sonnet-4-6"
            // Agente de ejemplo
            AGENTE analista {
              contexto = "Eres un analista de datos experto"
              PREGUNTAR "Cuales son las tendencias?" SOBRE ventas -> tendencias
              RESUMIR tendencias EN 100 palabras -> resumen
            }
            """;

    private final CodeArea area = new CodeArea(MUESTRA);

    AreaEditor() {
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));

        // Solo de muestra
        pintar("@modelo", "pz-directiva");
        pintar("\"claude-sonnet-4-6\"", "pz-cadena");
        pintar("// Agente de ejemplo", "pz-comentario");
        pintar("AGENTE", "pz-reservada");
        pintar("contexto", "pz-reservada");
        pintar("PREGUNTAR", "pz-comando");
        pintar("RESUMIR", "pz-comando");
        pintar("SOBRE", "pz-conector");
        pintar("->", "pz-conector");
        pintar("100", "pz-numero");

        FontIcon icono = Iconos.crear(Iconos.ARCHIVO_PZ);
        icono.getStyleClass().add("icono-pz");

        Tab pestana = new Tab("ejemplo.pz", new VirtualizedScrollPane<>(area));
        pestana.setGraphic(icono);
        pestana.setClosable(false);

        getTabs().add(pestana);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    }

    // Avisa (línea, columna) cada vez que se mueve el cursor. empieza por 1
    void setAlMoverCursor(BiConsumer<Integer, Integer> alMover) {
        Runnable avisar = () -> alMover.accept(area.getCurrentParagraph() + 1, area.getCaretColumn() + 1);
        area.caretPositionProperty().addListener((obs, antes, despues) -> avisar.run());
        avisar.run();
    }

    private void pintar(String fragmento, String clase) {
        int inicio = area.getText().indexOf(fragmento);
        if (inicio >= 0) {
            area.setStyle(inicio, inicio + fragmento.length(), List.of(clase));
        }
    }
}