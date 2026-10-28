package com.promptzal.backend.reportes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Esqueleto comun de los reportes: un solo archivo HTML, sin scripts.
final class PaginaHtml {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final String ESTILOS = """
            :root {
              color-scheme: dark light;
              --lienzo: #1E1F22; --panel: #2B2D30; --linea: #393B40;
              --texto: #DFE1E5; --silenciado: #9A9EA6; --acento: #3574F0;
              --error: #F75464; --aviso: #F2C55C; --exito: #5FB865;
              --directiva: #B3AE60; --reservada: #CF8E6D; --comando: #56A8F5;
              --conector: #C77DBB; --cadena: #6AAB73; --numero: #2AACB8;
            }
            @media (prefers-color-scheme: light), print {
              :root {
                --lienzo: #FFFFFF; --panel: #F2F3F5; --linea: #D5D7DB;
                --texto: #1E1F22; --silenciado: #5F636B; --acento: #2456C9;
                --error: #B3202E; --aviso: #8A6500; --exito: #2F7D32;
                --directiva: #7D7A1F; --reservada: #A24E1A; --comando: #1F6FC4;
                --conector: #8E3F8A; --cadena: #3F7D3A; --numero: #0E7C86;
              }
            }
            * { box-sizing: border-box; }
            body {
              margin: 0; background: var(--lienzo); color: var(--texto);
              font: 15px/1.5 Inter, system-ui, -apple-system, "Segoe UI", sans-serif;
            }
            main { max-width: 980px; margin: 0 auto; padding: 40px 24px 64px; }
            h1 { font-size: 28px; line-height: 1.2; font-weight: 650; margin: 0 0 16px; }
            .meta { display: grid; grid-template-columns: max-content 1fr; gap: 4px 16px; margin: 0 0 32px; }
            .meta dt { color: var(--silenciado); font-weight: 600; }
            .meta dd { margin: 0; }
            code, .arbol { font-family: "JetBrains Mono", ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 13px; }
            .resumen { list-style: none; display: flex; flex-wrap: wrap; gap: 12px; padding: 0; margin: 0 0 28px; }
            .resumen li { background: var(--panel); border: 1px solid var(--linea); border-radius: 6px; padding: 10px 16px; min-width: 130px; }
            .resumen b { display: block; font-size: 24px; line-height: 1.2; }
            .resumen span { color: var(--silenciado); font-size: 13px; }
            .tabla { overflow-x: auto; }
            table { width: 100%; border-collapse: collapse; font-size: 14px; }
            th, td { padding: 8px 12px; text-align: left; vertical-align: top; border-bottom: 1px solid var(--linea); }
            th { background: var(--panel); color: var(--silenciado); font-size: 13px; font-weight: 600; }
            .num { text-align: right; font-variant-numeric: tabular-nums; }
            .tipo { display: inline-block; padding: 1px 8px; border: 1px solid currentColor; border-radius: 4px; font-size: 12px; font-weight: 600; }
            .tipo-lexico { color: var(--aviso); }
            .tipo-sintactico { color: var(--error); }
            .tipo-semantico { color: var(--conector); }
            .vacio, .sin-errores { padding: 16px; background: var(--panel); border: 1px solid var(--linea); border-radius: 6px; }
            .sin-errores { border-color: var(--exito); }
            .arbol, .arbol ul { list-style: none; margin: 0; padding-left: 20px; }
            .arbol { padding-left: 0; }
            .arbol ul { margin-left: 6px; border-left: 1px solid var(--linea); }
            .arbol li { margin: 2px 0; }
            .arbol summary { cursor: pointer; }
            .arbol summary, .arbol .hoja { padding: 2px 4px; }
            .pos { margin-left: 12px; color: var(--silenciado); font-size: 12px; }
            .pz-directiva { color: var(--directiva); }
            .pz-reservada { color: var(--reservada); }
            .pz-comando { color: var(--comando); }
            .pz-conector { color: var(--conector); }
            """;

    private PaginaHtml() {
    }

    static String construir(String titulo, String archivo, LocalDateTime fecha, String cuerpo) {
        return construir(titulo, archivo, fecha, cuerpo, "");
    }

    static String construir(String titulo, String archivo, LocalDateTime fecha, String cuerpo, String estilosExtra) {
        return "<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
                + "<title>" + escapar(titulo) + " - " + escapar(archivo) + "</title>\n"
                + "<style>\n" + ESTILOS + estilosExtra + "</style>\n</head>\n<body>\n<main>\n"
                + "<h1>" + escapar(titulo) + "</h1>\n"
                + "<dl class=\"meta\"><dt>Archivo</dt><dd>" + escapar(archivo) + "</dd>"
                + "<dt>Compilado</dt><dd>" + FORMATO.format(fecha) + "</dd></dl>\n"
                + cuerpo
                + "</main>\n</body>\n</html>\n";
    }

    // Evita que un texto del usuario (por ejemplo "<b>") se interprete como HTML.
    static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
