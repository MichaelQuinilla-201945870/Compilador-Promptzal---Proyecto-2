package com.promptzal.backend.reportes;

import com.promptzal.backend.ResultadoCompilacion;
import com.promptzal.backend.ast.ConstructorArbolAst;
import com.promptzal.backend.ast.NodoAst;
import com.promptzal.backend.errores.ErrorCompilador;
import com.promptzal.backend.errores.TipoError;
import com.promptzal.backend.semantico.Simbolo;
import com.promptzal.backend.semantico.TipoSimbolo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

// Genera los tres reportes HTML a partir del resultado de una compilacion
public final class GeneradorReportes {

    private GeneradorReportes() {
    }

    // Reporte de errores
    public static String errores(ResultadoCompilacion r, String archivo, LocalDateTime fecha) {
        List<ErrorCompilador> errores = r.errores();
        StringBuilder html = new StringBuilder("<ul class=\"resumen\">");
        contador(html, "Total de errores", errores.size());
        for (TipoError tipo : TipoError.values()) {
            contador(html, tipo.getEtiqueta() + "s", cantidad(errores, tipo));
        }
        html.append("</ul>\n");

        if (errores.isEmpty()) {
            html.append("<p class=\"sin-errores\">La compilación no encontró errores.</p>\n");
        } else {
            html.append("""
                    <div class="tabla"><table>
                    <thead><tr>\
                    <th class="num">#</th><th>Tipo</th><th class="num">Línea</th>\
                    <th class="num">Columna</th><th>Descripción</th><th>Encontrado</th><th>Esperado</th>\
                    </tr></thead>
                    <tbody>
                    """);
            int numero = 1;
            for (ErrorCompilador e : errores) {
                html.append("<tr><td class=\"num\">").append(numero++).append("</td>")
                        .append("<td><span class=\"tipo tipo-")
                        .append(e.getTipo().name().toLowerCase(Locale.ROOT)).append("\">")
                        .append(PaginaHtml.escapar(e.getTipo().getEtiqueta())).append("</span></td>")
                        .append("<td class=\"num\">").append(e.getFila()).append("</td>")
                        .append("<td class=\"num\">").append(e.getColumna()).append("</td>")
                        .append("<td>").append(PaginaHtml.escapar(e.getDescripcion())).append("</td>")
                        .append(celdaCodigo(e.getEncontrado()))
                        .append(celdaCodigo(e.getEsperado()))
                        .append("</tr>\n");
            }
            html.append("</tbody></table></div>\n");
        }
        return PaginaHtml.construir("Reporte de errores", archivo, fecha, html.toString());
    }

    // Reporte de la tabla de simbolos
    public static String simbolos(ResultadoCompilacion r, String archivo, LocalDateTime fecha) {
        if (r.tabla() == null) {
            return PaginaHtml.construir("Tabla de símbolos", archivo, fecha,
                    "<p class=\"vacio\">No se generó la tabla de símbolos porque el archivo tiene "
                            + "errores sintácticos. Corrígelos y vuelve a compilar.</p>\n");
        }
        List<Simbolo> simbolos = r.tabla().getSimbolos();

        StringBuilder html = new StringBuilder("<ul class=\"resumen\">");
        contador(html, "Agentes", cantidad(simbolos, TipoSimbolo.AGENTE));
        contador(html, "Variables declaradas", cantidad(simbolos, TipoSimbolo.VARIABLE));
        contador(html, "Resultados de comando", cantidad(simbolos, TipoSimbolo.RESULTADO));
        html.append("</ul>\n");

        html.append("""
                <div class="tabla"><table>
                <thead><tr>\
                <th class="num">#</th><th>Nombre</th><th>Tipo</th><th>Ámbito</th>\
                <th class="num">Línea de declaración</th></tr></thead>
                <tbody>
                """);
        int numero = 1;
        for (Simbolo s : simbolos) {
            html.append("<tr><td class=\"num\">").append(numero++).append("</td>")
                    .append("<td><code>").append(PaginaHtml.escapar(s.nombre())).append("</code></td>")
                    .append("<td>").append(PaginaHtml.escapar(s.tipo().getEtiqueta())).append("</td>")
                    .append("<td>").append(PaginaHtml.escapar(s.ambito())).append("</td>")
                    .append("<td class=\"num\">").append(s.fila()).append("</td></tr>\n");
        }
        html.append("</tbody></table></div>\n");
        return PaginaHtml.construir("Tabla de símbolos", archivo, fecha, html.toString());
    }

    // Reporte del AST
    public static String ast(ResultadoCompilacion r, String archivo, LocalDateTime fecha) {
        if (r.programa() == null || r.tabla() == null) {
            return PaginaHtml.construir("Árbol de sintaxis abstracta (AST)", archivo, fecha,
                    "<p class=\"vacio\">No se generó el árbol porque el archivo tiene errores "
                            + "sintácticos. Corrígelos y vuelve a compilar.</p>\n");
        }
        StringBuilder html = new StringBuilder("<ul class=\"arbol\">\n");
        nodo(html, ConstructorArbolAst.construir(r.programa()));
        html.append("</ul>\n");
        return PaginaHtml.construir("Árbol de sintaxis abstracta (AST)", archivo, fecha, html.toString());
    }

    // Un nodo con hijos es un bloque desplegable; uno sin hijos es una linea
    private static void nodo(StringBuilder html, NodoAst nodo) {
        String clase = nodo.categoria() == null ? "" : " class=\"" + nodo.categoria() + "\"";
        String linea = "<span" + clase + ">" + PaginaHtml.escapar(nodo.texto()) + "</span>"
                + (nodo.fila() > 0
                ? "<span class=\"pos\">línea " + nodo.fila() + ", columna " + nodo.columna() + "</span>"
                : "");

        html.append("<li>");
        if (nodo.hijos().isEmpty()) {
            html.append("<div class=\"hoja\">").append(linea).append("</div>");
        } else {
            html.append("<details open><summary>").append(linea).append("</summary>\n<ul>\n");
            for (NodoAst hijo : nodo.hijos()) {
                nodo(html, hijo);
            }
            html.append("</ul>\n</details>");
        }
        html.append("</li>\n");
    }

    // Utilidades
    private static void contador(StringBuilder html, String etiqueta, long cantidad) {
        html.append("<li><b>").append(cantidad).append("</b><span>")
                .append(PaginaHtml.escapar(etiqueta)).append("</span></li>");
    }

    private static long cantidad(List<ErrorCompilador> errores, TipoError tipo) {
        return errores.stream().filter(e -> e.getTipo() == tipo).count();
    }

    private static long cantidad(List<Simbolo> simbolos, TipoSimbolo tipo) {
        return simbolos.stream().filter(s -> s.tipo() == tipo).count();
    }

    // Celda con texto en tipografia de codigo, o un guion si no aplica a este tipo de error
    private static String celdaCodigo(String texto) {
        if (texto == null || texto.isBlank()) {
            return "<td>—</td>";
        }
        return "<td><code>" + PaginaHtml.escapar(texto) + "</code></td>";
    }
}