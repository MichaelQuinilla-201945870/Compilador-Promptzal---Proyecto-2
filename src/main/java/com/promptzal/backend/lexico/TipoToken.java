package com.promptzal.backend.lexico;

// Catálogo nuevo de los tokens para la gramática del Proyecto 2

public enum TipoToken {
    DIRECTIVA,
    AGENTE,
    CONTEXTO,
    VARIABLE,
    EJECUTAR,
    EXPORTAR,
    COMANDO_IA,
    CARGAR,
    CONECTOR,
    FLECHA,
    IGUAL,
    MAS,
    LLAVE_A,
    LLAVE_C,
    PAR_A,
    PAR_C,
    COMA,
    COMILLA,
    ID,
    CADENA,
    NUMERO,
    EOF // Requerido por la gramática para marcar el fin del archivo
}
