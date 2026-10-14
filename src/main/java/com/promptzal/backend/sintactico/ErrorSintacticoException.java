package com.promptzal.backend.sintactico;

import com.promptzal.backend.errores.ErrorCompilador;

public class ErrorSintacticoException extends RuntimeException {

    private final ErrorCompilador error;

    // Se lanza cuando el parser encuentra un token que la gramática no esperaba.
    public ErrorSintacticoException(ErrorCompilador error) {
        super(error.getDescripcion());
        this.error = error;
    }

    public ErrorCompilador getError() {
        return error;
    }
}
