package com.promptzal.backend.sintactico;

import com.promptzal.backend.ast.ImpresorAST;
import com.promptzal.backend.ast.Programa;
import com.promptzal.backend.errores.GestorErrores;
import com.promptzal.backend.lexico.AnalizadorLexico;
import com.promptzal.backend.lexico.TipoToken;
import com.promptzal.backend.lexico.Token;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PruebaParser {
    public static void main(String[] args) throws IOException {
        String fuente = """
                @modelo "claude-sonnet-4-6"
                @rol "analista de datos"
                @formato "markdown"
                // Agente que prepara el analisis de un archivo de ventas
                AGENTE analista {
                 contexto = "Eres un analista de datos experto"
                 variable ventas = CARGAR("ventas.csv") PREGUNTAR "Cuales son las 3 tendencias principales?" SOBRE ventas -> tendencias
                 RESUMIR tendencias EN 100 palabras -> resumen
                }
                EJECUTAR analista
                EXPORTAR resumen
                """;

        GestorErrores gestor = new GestorErrores();
        AnalizadorLexico lexer = new AnalizadorLexico(new StringReader(fuente), gestor);

        List<Token> tokens = new ArrayList<>();
        Token t;
        do {
            t = lexer.siguienteToken();
            tokens.add(t);
        } while (t.getTipo() != TipoToken.EOF);

        Programa programa = new Parser(tokens, gestor).analizar();

        if (programa != null) {
            System.out.println(ImpresorAST.imprimir(programa));
        }
        System.out.println("--- Errores ---");
        gestor.getErrores().forEach(System.out::println);
    }
}