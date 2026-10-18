package com.promptzal.backend.semantico;

import com.promptzal.backend.Compilador;
import com.promptzal.backend.ResultadoCompilacion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PruebaSemantica {
    public static void main(String[] args) throws IOException {
        String archivo = "ejemplos/04_falta_llave.pz";     // <-- cambia este nombre para probar otro caso
        String fuente = Files.readString(Path.of(archivo));

        ResultadoCompilacion r = new Compilador().compilar(fuente);

        System.out.println("Archivo: " + archivo);
        if (r.tabla() == null) {
            System.out.println("Tabla de símbolos: no se generó (hay errores sintácticos)");
        } else {
            System.out.println("--- Tabla de símbolos ---");
            System.out.printf("%-22s %-12s %-10s %s%n", "TIPO", "NOMBRE", "ÁMBITO", "LÍNEA");
            for (Simbolo s : r.tabla().getSimbolos()) {
                System.out.printf("%-22s %-12s %-10s %d%n",
                        s.tipo().getEtiqueta(), s.nombre(), s.ambito(), s.fila());
            }
        }
        System.out.println("--- Errores (" + r.errores().size() + ") ---");
        r.errores().forEach(System.out::println);
    }
}
