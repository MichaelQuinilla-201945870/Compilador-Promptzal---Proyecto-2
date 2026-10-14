package com.promptzal.backend.ast;

public sealed interface Termino permits TerminoCadena, TerminoId, TerminoNumero, TerminoCarga{

    int fila();
    int columna();
}
