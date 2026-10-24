package com.promptzal.frontend.ui;

import com.promptzal.backend.lexico.TipoToken;
import com.promptzal.backend.lexico.Token;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

// Calcula los colores del editor a partir de los tokens que reconoce el lexer
final class Resaltador {

    private static final Collection<String> NORMAL = Collections.emptyList();
    private static final Collection<String> COMENTARIO = List.of("pz-comentario");
    private static final Collection<String> ERROR_LEXICO = List.of("pz-error-lexico");

    private Resaltador() {
    }

    //marcas: posiciones "fila:columna" de tokens con error de la ultima compilacion esos tokens se subrayan ademas de colorearse.
    static StyleSpans<Collection<String>> calcular(String texto, List<Token> tokens, Set<String> marcas) {
        int[] lineas = iniciosDeLinea(texto);
        StyleSpansBuilder<Collection<String>> estilos = new StyleSpansBuilder<>();
        int cubierto = 0;                                   // hasta donde ya se asigno un estilo

        for (Token token : tokens) {
            if (token.getTipo() == TipoToken.EOF) {
                break;
            }
            int inicio = posicion(lineas, token.getFila(), token.getColumna());
            int fin = inicio + token.getLexema().length();
            if (inicio < cubierto || fin > texto.length() || fin == inicio) {
                continue;                                   // posicion inconsistente: se omite el token
            }
            hueco(texto, cubierto, inicio, estilos);
            estilos.add(estiloDe(token, marcas), fin - inicio);
            cubierto = fin;
        }
        hueco(texto, cubierto, texto.length(), estilos);
        return estilos.create();
    }

    private static Collection<String> estiloDe(Token token, Set<String> marcas) {
        String categoria = CategoriaToken.de(token.getTipo());
        boolean marcado = marcas.contains(token.getFila() + ":" + token.getColumna());
        if (categoria == null && !marcado) {
            return NORMAL;
        }
        List<String> clases = new ArrayList<>(2);
        if (categoria != null) {
            clases.add(categoria);
        }
        if (marcado) {
            clases.add("pz-error");
        }
        return clases;
    }

    // Texto entre dos tokens: el lexer solo descarta espacios, comentarios y caracteres invalidos, así que cada parte se reconoce por su primer caracter.

    private static void hueco(String texto, int desde, int hasta, StyleSpansBuilder<Collection<String>> estilos) {
        int i = desde;
        while (i < hasta) {
            char c = texto.charAt(i);
            int j = i;
            if (esEspacio(c)) {
                while (j < hasta && esEspacio(texto.charAt(j))) {
                    j++;
                }
                estilos.add(NORMAL, j - i);
            } else if (c == '/' && i + 1 < hasta && texto.charAt(i + 1) == '/') {
                while (j < hasta && texto.charAt(j) != '\n') {
                    j++;
                }
                estilos.add(COMENTARIO, j - i);
            } else if (c == '/' && i + 1 < hasta && texto.charAt(i + 1) == '*') {
                int cierre = texto.indexOf("*/", i + 2);
                j = (cierre < 0 || cierre + 2 > hasta) ? hasta : cierre + 2;   // sin cerrar: hasta el final
                estilos.add(COMENTARIO, j - i);
            } else {
                j = i + 1;                                  // caracter que el lexer descarto como error
                estilos.add(ERROR_LEXICO, 1);
            }
            i = j;
        }
    }

    // Mismos espacios que reconoce el lexer
    private static boolean esEspacio(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private static int[] iniciosDeLinea(String texto) {
        List<Integer> inicios = new ArrayList<>();
        inicios.add(0);
        for (int i = 0; i < texto.length(); i++) {
            if (texto.charAt(i) == '\n') {
                inicios.add(i + 1);
            }
        }
        return inicios.stream().mapToInt(Integer::intValue).toArray();
    }

    // Posicion absoluta de (fila, columna), contando desde 1; -1 si la fila no existe
    private static int posicion(int[] lineas, int fila, int columna) {
        if (fila < 1 || fila > lineas.length) {
            return -1;
        }
        return lineas[fila - 1] + (columna - 1);
    }
}
