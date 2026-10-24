package com.promptzal.backend;

import com.promptzal.backend.ast.Programa;
import com.promptzal.backend.errores.GestorErrores;
import com.promptzal.backend.lexico.AnalizadorLexico;
import com.promptzal.backend.lexico.TipoToken;
import com.promptzal.backend.lexico.Token;
import com.promptzal.backend.semantico.AnalizadorSemantico;
import com.promptzal.backend.semantico.TablaSimbolos;
import com.promptzal.backend.sintactico.Parser;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import com.promptzal.backend.generador.GeneradorPromptzal;

//Ejecuta las fases en orden: léxico, sintáctico y semántico.
public class Compilador {

    public ResultadoCompilacion compilar(String fuente) {
        GestorErrores gestor = new GestorErrores();

        List<Token> tokens = tokenizar(fuente, gestor);

        Parser parser = new Parser(tokens, gestor);
        Programa programa = parser.analizar();


        // Un error semántico NO impide generar el prompt: se reporta y se sigue.

        TablaSimbolos tabla = null;
        String promptzal = null;
        if (programa != null && !parser.huboErrores()) {
            tabla = new AnalizadorSemantico(gestor).analizar(programa);
            promptzal = new GeneradorPromptzal().generar(programa);
        }

        return new ResultadoCompilacion(tokens, programa, tabla, promptzal,
                List.copyOf(gestor.getErrores()));
    }

    private List<Token> tokenizar(String fuente, GestorErrores gestor) {
        AnalizadorLexico lexer = new AnalizadorLexico(new StringReader(fuente), gestor);
        List<Token> tokens = new ArrayList<>();
        try {
            Token t;
            do {
                t = lexer.siguienteToken();
                tokens.add(t);
            } while (t.getTipo() != TipoToken.EOF);
        } catch (IOException e) {
            throw new UncheckedIOException(e);   // con un StringReader no debería ocurrir
        }
        return tokens;
    }

    // Solo la fase lexica (para el resaltado del editor)
    public List<Token> tokenizar(String fuente) {
        return tokenizar(fuente, new GestorErrores());

    }
}