package com.promptzal.frontend.ui;

import com.promptzal.backend.ast.Agente;
import com.promptzal.backend.ast.Comando;
import com.promptzal.backend.ast.Conector;
import com.promptzal.backend.ast.Directiva;
import com.promptzal.backend.ast.ImpresorAST;
import com.promptzal.backend.ast.Programa;
import com.promptzal.backend.ast.Referencia;
import com.promptzal.backend.ast.Variable;
import javafx.scene.control.TreeItem;

// Arma el arbol que muestra la pestaña AST a partir del AST del compilador
final class ArbolAst {

    // Una linea de arbol, fila = 0 significa "sin posición": el clic no hace nada
    record Nodo(String texto, String clase, int fila, int columna) { }

    private ArbolAst() {
    }

    static TreeItem<Nodo> construir(Programa programa) {
        TreeItem<Nodo> raiz = nodo("Programa", null, 0, 0);

        for (Directiva d : programa.directivas()) {
            raiz.getChildren().add(nodo(
                    "Directiva " + d.nombre() + " = \"" + d.valor() + "\"",
                    "pz-directiva", d.fila(), d.columna()));
        }
        for (Agente a : programa.agentes()) {
            raiz.getChildren().add(agente(a));
        }
        for (Referencia r : programa.ejecuciones()) {
            raiz.getChildren().add(nodo("Ejecutar " + r.nombre(), "pz-reservada", r.fila(), r.columna()));
        }
        TreeItem<Nodo> exportar = nodo("Exportar", "pz-reservada", 0, 0);
        for (Referencia r : programa.exportados()) {
            exportar.getChildren().add(nodo(r.nombre(), null, r.fila(), r.columna()));
        }
        raiz.getChildren().add(exportar);

        expandir(raiz);
        return raiz;
    }

    private static TreeItem<Nodo> agente(Agente a) {
        TreeItem<Nodo> item = nodo("Agente " + a.nombre(), "pz-reservada", a.fila(), a.columna());
        item.getChildren().add(nodo("Contexto: \"" + a.contexto() + "\"", null, 0, 0));

        for (Variable v : a.variables()) {
            TreeItem<Nodo> variable = nodo("Variable " + v.nombre(), "pz-reservada", v.fila(), v.columna());
            variable.getChildren().add(nodo(
                    "Valor: " + ImpresorAST.expresion(v.valor()), null, v.fila(), v.columna()));
            item.getChildren().add(variable);
        }
        for (Comando c : a.comandos()) {
            item.getChildren().add(comando(c));
        }
        return item;
    }

    private static TreeItem<Nodo> comando(Comando c) {
        TreeItem<Nodo> item = nodo("Comando " + c.tipo(), "pz-comando", c.fila(), c.columna());
        item.getChildren().add(nodo(
                "Argumento: " + ImpresorAST.expresion(c.argumento()), null, c.fila(), c.columna()));
        for (Conector k : c.conectores()) {
            item.getChildren().add(nodo(
                    k.tipo() + ": " + ImpresorAST.expresion(k.argumento()), "pz-conector", k.fila(), k.columna()));
        }
        item.getChildren().add(nodo(
                "Destino: " + c.destino().nombre(), null, c.destino().fila(), c.destino().columna()));
        return item;
    }

    private static TreeItem<Nodo> nodo(String texto, String clase, int fila, int columna) {
        return new TreeItem<>(new Nodo(texto, clase, fila, columna));
    }

    private static void expandir(TreeItem<Nodo> item) {
        item.setExpanded(true);
        for (TreeItem<Nodo> hijo : item.getChildren()) {
            expandir(hijo);
        }
    }
}