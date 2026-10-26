package com.promptzal.backend.ast;

// Convierte el AST en un arbol de lineas de texto, con la posicion de cada una
public final class ConstructorArbolAst {

    private ConstructorArbolAst() {
    }

    public static NodoAst construir(Programa programa) {
        NodoAst raiz = new NodoAst("Programa", null, 0, 0);

        for (Directiva d : programa.directivas()) {
            raiz.hijos().add(new NodoAst(
                    "Directiva " + d.nombre() + " = \"" + d.valor() + "\"",
                    "pz-directiva", d.fila(), d.columna()));
        }
        for (Agente a : programa.agentes()) {
            raiz.hijos().add(agente(a));
        }
        for (Referencia r : programa.ejecuciones()) {
            raiz.hijos().add(new NodoAst("Ejecutar " + r.nombre(), "pz-reservada", r.fila(), r.columna()));
        }
        NodoAst exportar = new NodoAst("Exportar", "pz-reservada", 0, 0);
        for (Referencia r : programa.exportados()) {
            exportar.hijos().add(new NodoAst(r.nombre(), null, r.fila(), r.columna()));
        }
        raiz.hijos().add(exportar);
        return raiz;
    }

    private static NodoAst agente(Agente a) {
        NodoAst nodo = new NodoAst("Agente " + a.nombre(), "pz-reservada", a.fila(), a.columna());
        nodo.hijos().add(new NodoAst("Contexto: \"" + a.contexto() + "\"", null, 0, 0));

        for (Variable v : a.variables()) {
            NodoAst variable = new NodoAst("Variable " + v.nombre(), "pz-reservada", v.fila(), v.columna());
            variable.hijos().add(new NodoAst(
                    "Valor: " + ImpresorAST.expresion(v.valor()), null, v.fila(), v.columna()));
            nodo.hijos().add(variable);
        }
        for (Comando c : a.comandos()) {
            nodo.hijos().add(comando(c));
        }
        return nodo;
    }

    private static NodoAst comando(Comando c) {
        NodoAst nodo = new NodoAst("Comando " + c.tipo(), "pz-comando", c.fila(), c.columna());
        nodo.hijos().add(new NodoAst(
                "Argumento: " + ImpresorAST.expresion(c.argumento()), null, c.fila(), c.columna()));
        for (Conector k : c.conectores()) {
            nodo.hijos().add(new NodoAst(
                    k.tipo() + ": " + ImpresorAST.expresion(k.argumento()), "pz-conector", k.fila(), k.columna()));
        }
        nodo.hijos().add(new NodoAst(
                "Destino: " + c.destino().nombre(), null, c.destino().fila(), c.destino().columna()));
        return nodo;
    }
}