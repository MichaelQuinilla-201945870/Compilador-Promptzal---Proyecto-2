package com.promptzal.frontend.tema;

import org.kordamp.ikonli.javafx.FontIcon;

// Catalogo de iconos (Material Design, vía Ikonli).
public final class Iconos {

    public static final String COMPILAR = "mdi2p-play";
    public static final String ABRIR = "mdi2f-folder-open-outline";
    public static final String GUARDAR = "mdi2c-content-save-outline";
    public static final String COPIAR = "mdi2c-content-copy";
    public static final String OCULTAR = "mdi2m-minus";

    public static final String PROYECTO = "mdi2f-folder-outline";
    public static final String PROMPTZAL = "mdi2t-text-box-outline";
    public static final String ARCHIVO_PZ = "mdi2f-file-document-outline";

    public static final String ERRORES = "mdi2a-alert-circle-outline";
    public static final String TOKENS = "mdi2f-format-list-bulleted";
    public static final String SIMBOLOS = "mdi2t-table";
    public static final String AST = "mdi2f-file-tree";

    private Iconos() {
    }

    public static FontIcon crear(String codigo) {
        return new FontIcon(codigo);
    }
}