package com.promptzal.backend.ast;

public record Variable(String nombre, Expresion valor, int fila, int columna) {
}
