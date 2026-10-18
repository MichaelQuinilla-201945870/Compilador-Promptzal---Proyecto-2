package com.promptzal.backend;

import com.promptzal.backend.ast.Programa;
import com.promptzal.backend.errores.ErrorCompilador;
import com.promptzal.backend.lexico.Token;
import com.promptzal.backend.semantico.TablaSimbolos;
import java.util.List;

public record ResultadoCompilacion(List<Token> tokens, Programa programa,
                                   TablaSimbolos tabla, List<ErrorCompilador> errores) {
}
