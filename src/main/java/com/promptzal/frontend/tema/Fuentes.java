package com.promptzal.frontend.tema;

import javafx.scene.text.Font;
import java.io.IOException;
import java.io.InputStream;

// Carga las fuentes incluidas en el proyecto
public final class Fuentes {

    private static final String[] ARCHIVOS = {
            "JetBrainsMono-Regular.ttf", "JetBrainsMono-Bold.ttf",
            "JetBrainsMono-Italic.ttf", "JetBrainsMono-BoldItalic.ttf",
            "Inter-Regular.ttf", "Inter-Bold.ttf"
    };

    private Fuentes() {
    }

    public static void cargar() {
        for (String archivo : ARCHIVOS) {
            try (InputStream in = Fuentes.class.getResourceAsStream("/fonts/" + archivo)) {
                if (in == null || Font.loadFont(in, 13) == null) {
                    System.err.println("No se pudo cargar la fuente: " + archivo);
                }
            } catch (IOException e) {
                System.err.println("Error al leer la fuente " + archivo + ": " + e.getMessage());
            }
        }
    }

    public static boolean disponible(String familia) {
        return Font.getFamilies().contains(familia);
    }
}