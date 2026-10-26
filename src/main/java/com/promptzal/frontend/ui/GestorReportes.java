package com.promptzal.frontend.ui;

import com.promptzal.backend.ResultadoCompilacion;
import com.promptzal.backend.reportes.GeneradorReportes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

// Escribe en la carpeta reportes/ los reportes de la ultima compilacion
final class GestorReportes {

    enum Tipo {
        ERRORES("errores"), SIMBOLOS("simbolos"), AST("ast");

        private final String sufijo;

        Tipo(String sufijo) {
            this.sufijo = sufijo;
        }

        String sufijo() {
            return sufijo;
        }
    }

    private static final Path CARPETA = Path.of("reportes");

    private ResultadoCompilacion resultado;
    private String archivo = "";
    private LocalDateTime fecha = LocalDateTime.now();

    // Se llama en cada compilacion: los reportes siempre salen de la ultima
    void actualizar(ResultadoCompilacion resultado, String archivo) {
        this.resultado = resultado;
        this.archivo = archivo;
        this.fecha = LocalDateTime.now();
    }

    Path generar(Tipo tipo) throws IOException {
        String html = switch (tipo) {
            case ERRORES -> GeneradorReportes.errores(resultado, archivo, fecha);
            case SIMBOLOS -> GeneradorReportes.simbolos(resultado, archivo, fecha);
            case AST -> GeneradorReportes.ast(resultado, archivo, fecha);
        };
        Files.createDirectories(CARPETA);
        Path destino = CARPETA.resolve(nombreBase() + "_" + tipo.sufijo() + ".html").toAbsolutePath();
        Files.writeString(destino, html);                    // UTF-8
        return destino;
    }

    Path carpeta() {
        return CARPETA.toAbsolutePath();
    }

    private String nombreBase() {
        return archivo.endsWith(".pz") ? archivo.substring(0, archivo.length() - 3) : archivo;
    }
}
