package com.promptzal.backend.sintactico;

import com.promptzal.backend.ast.ImpresorAST;
import com.promptzal.backend.ast.Programa;
import com.promptzal.backend.errores.GestorErrores;
import com.promptzal.backend.lexico.AnalizadorLexico;
import com.promptzal.backend.lexico.TipoToken;
import com.promptzal.backend.lexico.Token;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PruebaParser {
    public static void main(String[] args) throws IOException {
        String archivo = "ejemplos/07_basura.pz";     // <-- cambia este nombre para probar otro caso
        String fuente = Files.readString(Path.of(archivo));

        GestorErrores gestor = new GestorErrores();
        AnalizadorLexico lexer = new AnalizadorLexico(new StringReader(fuente), gestor);

        List<Token> tokens = new ArrayList<>();
        Token t;
        do {
            t = lexer.siguienteToken();
            tokens.add(t);
        } while (t.getTipo() != TipoToken.EOF);

        Parser parser = new Parser(tokens, gestor);
        Programa programa = parser.analizar();

        System.out.println("Archivo: " + archivo);
        if (programa != null) {
            System.out.println(ImpresorAST.imprimir(programa));
        }
        System.out.println("--- Errores (" + gestor.getErrores().size() + ") ---");
        gestor.getErrores().forEach(System.out::println);
    }
}