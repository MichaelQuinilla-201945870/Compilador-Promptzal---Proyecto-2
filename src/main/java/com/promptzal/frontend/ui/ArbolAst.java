package com.promptzal.frontend.ui;

import com.promptzal.backend.ast.ConstructorArbolAst;
import com.promptzal.backend.ast.NodoAst;
import com.promptzal.backend.ast.Programa;
import javafx.scene.control.TreeItem;

//ahora convierte el arbol del backend en el arbol que muestra la pestaña AST
final class ArbolAst {

    // Una linea de arbol, fila = 0 significa "sin posición": el clic no hace nada
    record Nodo(String texto, String clase, int fila, int columna) { }

    private ArbolAst() {
    }

    static TreeItem<Nodo> construir(Programa programa) {
        return convertir(ConstructorArbolAst.construir(programa));
    }

    private static TreeItem<Nodo> convertir(NodoAst nodo) {
        TreeItem<Nodo> item = new TreeItem<>(
                new Nodo(nodo.texto(), nodo.categoria(), nodo.fila(), nodo.columna()));
        for (NodoAst hijo : nodo.hijos()) {
            item.getChildren().add(convertir(hijo));
        }
        item.setExpanded(true);
        return item;
    }
}