package com.promptzal.backend.ast;

import java.util.ArrayList;
import java.util.List;

//Una linea del arbol del AST, lista para mostrarse (en la interfaz o en un reporte)
public record NodoAst(String texto, String categoria, int fila, int columna, List<NodoAst> hijos) {

    public NodoAst(String texto, String categoria, int fila, int columna) {
        this(texto, categoria, fila, columna, new ArrayList<>());
    }
}