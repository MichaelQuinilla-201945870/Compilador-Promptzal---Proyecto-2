package com.promptzal.backend.lexico;


//Nueva clase de token

public class Token {

    private final TipoToken tipo;   // clasificación del token
    private final String lexema;    // texto exacto tomado del archivo
    private final int fila;         // empieza en 1
    private final int columna;      // empieza en 1

    public Token(TipoToken tipo, String lexema, int fila, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.fila = fila;
        this.columna = columna;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return tipo + "\t" + lexema + "\t" + fila + "\t" + columna;
    }
}