package com.promptzal.backend.reportes;

import com.promptzal.backend.afd.Afd;
import com.promptzal.backend.afd.EstadoAfd;
import com.promptzal.backend.lexico.Token;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// Reporte del autómata: un diagrama SVG dentro de una pagina HTML.
// Se arma en cada compilacion a partir del vocabulario real

final class ReporteAfd {

    private static final double RADIO = 10;
    private static final double RADIO_INICIAL = 15;
    private static final double ANCHO_COLUMNA = 72;
    private static final double PRIMER_SALTO = 140;      // la primera columna lleva etiquetas mas largas
    private static final double ALTO_FILA = 34;
    private static final double MARGEN_X = 28;
    private static final double MARGEN_Y = 36;
    private static final double ESPACIO_DERECHO = 150;   // para el nombre del token de cada estado final

    private static final String ESTILOS = """
            .diagrama { overflow-x: auto; background: var(--panel); border: 1px solid var(--linea);
                        border-radius: 6px; padding: 8px; margin-bottom: 24px; }
            .afd { display: block; color: var(--texto);
                   font-family: "JetBrains Mono", ui-monospace, SFMono-Regular, Menlo, monospace; }
            .afd text { fill: currentColor; font-size: 11px; }
            .afd .arco { fill: none; stroke: var(--silenciado); stroke-width: 1.2; }
            .afd .bucle { fill: none; stroke: var(--silenciado); stroke-width: 1.2; }
            .afd .punta { fill: var(--silenciado); }
            .afd .letra { fill: var(--texto); }
            .afd .estado { color: var(--texto); }
            .afd .estado.pz-directiva { color: var(--directiva); }
            .afd .estado.pz-reservada { color: var(--reservada); }
            .afd .estado.pz-comando { color: var(--comando); }
            .afd .estado.pz-conector { color: var(--conector); }
            .afd .estado.pz-cadena { color: var(--cadena); }
            .afd .estado.pz-numero { color: var(--numero); }
            .afd .estado circle { fill: var(--lienzo); stroke: currentColor; stroke-width: 1.4; }
            .afd .estado:not(.acepta) circle { stroke: var(--silenciado); }
            .afd .estado .interno { fill: none; }
            .afd .estado.visitado .externo { fill: var(--acento); fill-opacity: .4; }
            .afd .token { font-weight: 600; }
            .notas { padding-left: 20px; }
            .notas li { margin: 4px 0; }
            """;

    private final Afd afd;
    private final Set<EstadoAfd> alcanzados;
    private final Map<EstadoAfd, double[]> posicion = new HashMap<>();   // estado -> {x, y}
    private final StringBuilder svg = new StringBuilder();
    private double siguienteFila = 0;
    private int profundidadMaxima = 0;

    private ReporteAfd(Afd afd, Set<EstadoAfd> alcanzados) {
        this.afd = afd;
        this.alcanzados = alcanzados;
    }

    static String generar(List<Token> tokens, String archivo, LocalDateTime fecha) {
        Afd afd = Afd.construir();
        return new ReporteAfd(afd, afd.alcanzados(tokens)).pagina(archivo, fecha);
    }

    // Pagina
    private String pagina(String archivo, LocalDateTime fecha) {
        colocar(afd.inicial(), 0, false);
        dibujar();

        StringBuilder html = new StringBuilder("<ul class=\"resumen\">");
        contador(html, "Estados", afd.estados().size());
        contador(html, "Estados de aceptación", afd.cantidadDeAceptacion());
        contador(html, "Alcanzados por este archivo", alcanzados.size());
        html.append("</ul>\n");

        html.append("<div class=\"diagrama\">").append(svg).append("</div>\n");
        html.append("<h2>Cómo leer el diagrama</h2>\n<ul class=\"notas\">\n");
        nota(html, "Doble círculo: estado de aceptación. A su lado va el tipo de token que reconoce.");
        nota(html, "Relleno de color: el archivo compilado terminó al menos un token en ese estado.");
        nota(html, "Cada palabra del lenguaje tiene su propio camino de estados. Las palabras que empiezan "
                + "igual comparten los primeros estados.");
        nota(html, "Desde cualquier estado de una palabra, una letra, un dígito o _ que no continúa la palabra "
                + "lleva a qID (identificador). Por eso AGE o AGENTES son identificadores.");
        nota(html, "Cualquier otro carácter lleva al estado de error, que no se dibuja.");
        nota(html, "Los estados de los comentarios no producen token: al terminar (fin de línea o */) "
                + "el autómata vuelve a q0.");
        nota(html, "Una cadena produce tres tokens: la comilla de apertura, el contenido y la comilla de cierre. "
                + "La cadena vacía va directo de la primera comilla a la segunda.");
        html.append("</ul>\n<h2>Estados que se repiten</h2>\n<ul class=\"notas\">\n");
        for (EstadoAfd estado : afd.estados()) {
            if (estado.bucle() != null) {
                nota(html, "<code>" + PaginaHtml.escapar(estado.nombre()) + "</code> se queda en el mismo estado "
                        + "mientras lee: " + PaginaHtml.escapar(estado.bucle()));
            }
        }
        html.append("</ul>\n");
        return PaginaHtml.construir("Autómata finito determinista (AFD)", archivo, fecha, html.toString(), ESTILOS);
    }

    private static void contador(StringBuilder html, String etiqueta, long cantidad) {
        html.append("<li><b>").append(cantidad).append("</b><span>")
                .append(PaginaHtml.escapar(etiqueta)).append("</span></li>");
    }

    private static void nota(StringBuilder html, String texto) {
        html.append("<li>").append(texto).append("</li>\n");
    }

    // Distribucion: la profundidad da la columna y cada hoja ocupa una fila

    /* aire: algun estado de este tramo (sin bifurcaciones hasta la hoja) tiene un arco que se repite o
      un nombre de token debajo, y necesita espacio extra arriba y abajo para que nada se sobreponga. */
    private double colocar(EstadoAfd estado, int profundidad, boolean aire) {
        profundidadMaxima = Math.max(profundidadMaxima, profundidad);
        boolean necesitaAire = aire || estado.bucle() != null
                || (estado.acepta() != null && !estado.arcos().isEmpty());
        double fila;
        if (estado.arcos().isEmpty()) {
            if (necesitaAire) {
                siguienteFila += 0.5;
            }
            fila = siguienteFila++;
            if (necesitaAire) {
                siguienteFila += 0.5;
            }
        } else {
            boolean heredar = estado.arcos().size() == 1 && necesitaAire;   // solo un camino: comparte fila
            double primera = 0;
            double ultima = 0;
            for (int i = 0; i < estado.arcos().size(); i++) {
                double hija = colocar(estado.arcos().get(i).destino(), profundidad + 1, heredar);
                if (i == 0) {
                    primera = hija;
                }
                ultima = hija;
            }
            fila = (primera + ultima) / 2;
        }
        posicion.put(estado, new double[]{columna(profundidad), MARGEN_Y + fila * ALTO_FILA});
        return fila;
    }

    private static double columna(int profundidad) {
        double base = MARGEN_X + 44;
        return profundidad == 0 ? base : base + PRIMER_SALTO + (profundidad - 1) * ANCHO_COLUMNA;
    }

    // Dibujo
    private void dibujar() {
        double ancho = columna(profundidadMaxima) + RADIO + ESPACIO_DERECHO;
        double alto = MARGEN_Y * 2 + Math.max(0, siguienteFila - 1) * ALTO_FILA;

        svg.append("<svg class=\"afd\" xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(n(ancho))
                .append("\" height=\"").append(n(alto)).append("\" viewBox=\"0 0 ").append(n(ancho))
                .append(' ').append(n(alto)).append("\" role=\"img\" aria-label=\"Diagrama del autómata\">\n");
        svg.append("<defs><marker id=\"punta\" viewBox=\"0 0 10 10\" refX=\"9\" refY=\"5\" markerWidth=\"7\" "
                + "markerHeight=\"7\" orient=\"auto\"><path class=\"punta\" d=\"M0,0 L10,5 L0,10 z\"/></marker></defs>\n");

        double[] q0 = posicion.get(afd.inicial());
        svg.append("<path class=\"arco\" d=\"M").append(n(MARGEN_X)).append(' ').append(n(q0[1])).append(" L")
                .append(n(q0[0] - RADIO_INICIAL - 1)).append(' ').append(n(q0[1]))
                .append("\" marker-end=\"url(#punta)\"/>\n");

        for (EstadoAfd estado : afd.estados()) {
            for (EstadoAfd.Arco arco : estado.arcos()) {
                arco(estado, arco);
            }
        }
        for (EstadoAfd estado : afd.estados()) {
            estado(estado);
        }
        svg.append("</svg>");
    }

    private void arco(EstadoAfd origen, EstadoAfd.Arco arco) {
        double[] a = posicion.get(origen);
        double[] b = posicion.get(arco.destino());
        double x1 = a[0] + radio(origen);
        double x2 = b[0] - radio(arco.destino()) - 1;
        double y1 = a[1];
        double y2 = b[1];

        String trazo;
        if (Math.abs(y1 - y2) < 0.5) {
            trazo = "M" + n(x1) + " " + n(y1) + " L" + n(x2) + " " + n(y2);
        } else {
            double medio = (x1 + x2) / 2;
            trazo = "M" + n(x1) + " " + n(y1) + " C" + n(medio) + " " + n(y1) + " "
                    + n(medio) + " " + n(y2) + " " + n(x2) + " " + n(y2);
        }
        svg.append("<path class=\"arco\" d=\"").append(trazo).append("\" marker-end=\"url(#punta)\"/>\n");
        svg.append("<text class=\"letra\" text-anchor=\"end\" x=\"").append(n(x2 - 6)).append("\" y=\"")
                .append(n(y2 - 13)).append("\">").append(PaginaHtml.escapar(arco.simbolo())).append("</text>\n");
    }

    private void estado(EstadoAfd estado) {
        double[] p = posicion.get(estado);
        double r = radio(estado);
        boolean acepta = estado.acepta() != null;

        svg.append("<g class=\"estado");
        if (acepta) {
            svg.append(" acepta");
        }
        if (estado.clase() != null) {
            svg.append(' ').append(estado.clase());
        }
        if (alcanzados.contains(estado)) {
            svg.append(" visitado");
        }
        svg.append("\"><title>").append(PaginaHtml.escapar(titulo(estado))).append("</title>");
        svg.append("<circle class=\"externo\" cx=\"").append(n(p[0])).append("\" cy=\"").append(n(p[1]))
                .append("\" r=\"").append(n(r)).append("\"/>");
        if (acepta) {
            svg.append("<circle class=\"interno\" cx=\"").append(n(p[0])).append("\" cy=\"").append(n(p[1]))
                    .append("\" r=\"").append(n(r - 3.5)).append("\"/>");
        }
        if (estado == afd.inicial()) {
            svg.append("<text text-anchor=\"middle\" x=\"").append(n(p[0])).append("\" y=\"")
                    .append(n(p[1] + 4)).append("\">q0</text>");
        }
        if (acepta) {
            if (estado.arcos().isEmpty()) {                      // hoja: el tipo va a la derecha
                svg.append("<text class=\"token\" x=\"").append(n(p[0] + r + 7)).append("\" y=\"")
                        .append(n(p[1] + 4)).append("\">").append(estado.acepta()).append("</text>");
            } else {                                              // sigue más camino: el tipo va debajo
                svg.append("<text class=\"token\" text-anchor=\"middle\" x=\"").append(n(p[0])).append("\" y=\"")
                        .append(n(p[1] + r + 12)).append("\">").append(estado.acepta()).append("</text>");
            }
        }
        if (estado.bucle() != null) {                             // arco pequeño que vuelve al mismo estado
            svg.append("<path class=\"bucle\" d=\"M").append(n(p[0] - 6)).append(' ').append(n(p[1] - r + 1))
                    .append(" C").append(n(p[0] - 11)).append(' ').append(n(p[1] - r - 14)).append(' ')
                    .append(n(p[0] + 11)).append(' ').append(n(p[1] - r - 14)).append(' ')
                    .append(n(p[0] + 6)).append(' ').append(n(p[1] - r + 1))
                    .append("\" marker-end=\"url(#punta)\"/>");
        }
        svg.append("</g>\n");
    }

    private static String titulo(EstadoAfd estado) {
        StringBuilder texto = new StringBuilder(estado.nombre());
        if (estado.acepta() != null) {
            texto.append(": acepta ").append(estado.acepta());
        }
        if (estado.bucle() != null) {
            texto.append(" (se repite con: ").append(estado.bucle()).append(')');
        }
        return texto.toString();
    }

    private double radio(EstadoAfd estado) {
        return estado == afd.inicial() ? RADIO_INICIAL : RADIO;
    }

    // Numero con punto decimal, sin importar el idioma del sistema
    private static String n(double valor) {
        return String.format(Locale.ROOT, "%.1f", valor);
    }
}
