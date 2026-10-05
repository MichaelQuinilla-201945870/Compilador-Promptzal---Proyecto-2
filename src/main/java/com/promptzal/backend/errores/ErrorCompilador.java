package com.promptzal.backend.errores;

public class ErrorCompilador {

    private final TipoError tipo;
    private final String descripcion;
    private final String encontrado;   // lexema o token encontrado (null en semánticos)
    private final String esperado;     // solo en sintácticos (null en los demás)
    private final int fila;
    private final int columna;

    private ErrorCompilador(TipoError tipo, String descripcion, String encontrado, String esperado, int fila, int columna) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.encontrado = encontrado;
        this.esperado = esperado;
        this.fila = fila;
        this.columna = columna;
    }

    public static ErrorCompilador lexico(String lexema, String descripcion, int fila, int columna) {
        return new ErrorCompilador(TipoError.LEXICO, descripcion, lexema, null, fila, columna);
    }

    public static ErrorCompilador sintactico(String encontrado, String esperado, int fila, int columna) {
        String descripcion = "Se esperaba " + esperado + " pero se encontró " + encontrado;
        return new ErrorCompilador(TipoError.SINTACTICO, descripcion, encontrado, esperado, fila, columna);
    }

    public static ErrorCompilador semantico(String descripcion, int fila, int columna) {
        return new ErrorCompilador(TipoError.SEMANTICO, descripcion, null, null, fila, columna);
    }

    public TipoError getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEncontrado() {
        return encontrado;
    }

    public String getEsperado() {
        return esperado;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return tipo.getEtiqueta() + "\t" + descripcion + "\t" + fila + "\t" + columna;
    }
}
