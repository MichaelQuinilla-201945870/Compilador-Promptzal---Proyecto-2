package com.promptzal.backend;

import com.promptzal.backend.ast.Programa;
import com.promptzal.backend.errores.ErrorCompilador;
import com.promptzal.backend.lexico.Token;
import com.promptzal.backend.semantico.TablaSimbolos;
import java.util.List;

//lo que produce una compilacion. tabla y promptzal son null si hubo errores sintactico

public record ResultadoCompilacion(List<Token> tokens, Programa programa, TablaSimbolos tabla,
                                   String promptzal, List<ErrorCompilador> errores) {

}
