package com.promptzal.backend.ast;

public record TerminoNumero(String valor, String unidad, int fila, int columna) implements Termino {
}
