package com.promptzal.backend.lexico;

import com.promptzal.backend.errores.ErrorCompilador;
import com.promptzal.backend.errores.GestorErrores;

%%


%class AnalizadorLexico
%public
%unicode
%line
%column
%type Token
%function siguienteToken
%ctorarg GestorErrores gestorErrores

%init{
    this.gestorErrores = gestorErrores;
%init}

%{
    private GestorErrores gestorErrores;

    // Posición donde empezó la cadena o el comentario que está abierto
    private int filaInicio;
    private int columnaInicio;

    // JFlex cuenta desde 0; el proyecto cuenta desde 1
    private Token token(TipoToken tipo) {
        return new Token(tipo, yytext(), yyline + 1, yycolumn + 1);
    }

    private Token eof() {
        return new Token(TipoToken.EOF, "", yyline + 1, yycolumn + 1);
    }

    private void error(String lexema, String descripcion, int fila, int columna) {
        gestorErrores.agregar(ErrorCompilador.lexico(lexema, descripcion, fila, columna));
    }
%}

%xstate ESTADO_CADENA
%xstate ESTADO_COMENTARIO

LETRA   = [a-zA-Z_]
DIGITO  = [0-9]
ESPACIO = [ \t\r\n]+

%%

<YYINITIAL> {

    // Espacios y comentarios: se descartan, nunca llegan al parser

    {ESPACIO}               { /* se descarta */ }
    "//" [^\r\n]*           { /* comentario de línea */ }
    "/*"                    { filaInicio = yyline + 1; columnaInicio = yycolumn + 1;
                              yybegin(ESTADO_COMENTARIO); }


    // Directivas: la regla reconoce la forma (@ + palabra); el diccionario PalabrasClave decide si es válida

    "@" [a-zA-Z0-9_]* {
                                String nombre = yytext().substring(1);
                                if (PalabrasClave.esDirectivaValida(nombre)) {
                                    return token(TipoToken.DIRECTIVA);
                                }
                                error(yytext(), "Directiva desconocida", yyline + 1, yycolumn + 1);
                            }

    // Operadores y delimitadores (símbolos: cambian la forma del lenguaje)

    "->" { return token(TipoToken.FLECHA); }
    "=" { return token(TipoToken.IGUAL); }
    "+" { return token(TipoToken.MAS); }
    "{" { return token(TipoToken.LLAVE_A); }
    "}" { return token(TipoToken.LLAVE_C); }
    "(" { return token(TipoToken.PAR_A); }
    ")" { return token(TipoToken.PAR_C); }
    "," { return token(TipoToken.COMA); }

    //La comilla de apertura es un token; el contenido sale en otro estado

    \"  { filaInicio = yyline + 1; columnaInicio = yycolumn + 1; yybegin(ESTADO_CADENA); return token(TipoToken.COMILLA); }


    // Números

    {DIGITO}+ ("." {DIGITO}+)?      { return token(TipoToken.NUMERO); }


    // Palabras: el diccionario decide si es AGENTE, COMANDO_IA, CONECTOR, ID, etc.

    {LETRA} ({LETRA} | {DIGITO})*   { return token(PalabrasClave.tipoDePalabra(yytext())); }


    // Cualquier otro carácter es un error léxico; se descarta solo ese carácter

    [^] { error(yytext(), "Carácter no reconocido", yyline + 1, yycolumn + 1); }
}


<ESTADO_CADENA> {
    [^\"\r\n]+ { return token(TipoToken.CADENA); }
    \" { yybegin(YYINITIAL); return token(TipoToken.COMILLA); }
    \r | \n | \r\n { error("\"", "Cadena sin cerrar: falta la comilla de cierre",
                                    filaInicio, columnaInicio);
                              yybegin(YYINITIAL); }
}

<ESTADO_CADENA> <<EOF>> { error("\"", "Cadena sin cerrar: falta la comilla de cierre",
                                    filaInicio, columnaInicio);
                              yybegin(YYINITIAL);
                              return eof(); }


<ESTADO_COMENTARIO> {
    "*/"                    { yybegin(YYINITIAL); }
    [^*]+ | "*"             { /* se descarta el contenido del comentario */ }
}


<ESTADO_COMENTARIO> <<EOF>> { error("/*", "Comentario de bloque sin cerrar",
                                    filaInicio, columnaInicio);
                              yybegin(YYINITIAL);
                              return eof(); }


<YYINITIAL> <<EOF>> { return eof(); }







