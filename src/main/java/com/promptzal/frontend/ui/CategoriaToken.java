package com.promptzal.frontend.ui;

import com.promptzal.backend.lexico.TipoToken;
import java.util.List;

// Categoria de color de cada token. Un color significa una cosa en toda la interfaz
final class CategoriaToken {

    static final List<String> TODAS = List.of(
            "pz-directiva", "pz-reservada", "pz-comando", "pz-conector",
            "pz-cadena", "pz-numero", "pz-comentario");

    private CategoriaToken() {
    }

    // Clase CSS del color del token, o null si va en el color normal.
    static String de(TipoToken tipo) {
        return switch (tipo) {
            case DIRECTIVA -> "pz-directiva";
            case AGENTE, CONTEXTO, VARIABLE, EJECUTAR, EXPORTAR -> "pz-reservada";
            case COMANDO_IA, CARGAR -> "pz-comando";
            case CONECTOR, FLECHA -> "pz-conector";
            case COMILLA, CADENA -> "pz-cadena";
            case NUMERO -> "pz-numero";
            default -> null;
        };
    }
}