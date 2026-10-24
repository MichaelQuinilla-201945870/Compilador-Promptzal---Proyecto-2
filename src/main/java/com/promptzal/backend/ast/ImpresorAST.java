package com.promptzal.backend.ast;
import java.util.stream.Collectors;


public final class ImpresorAST {

    private ImpresorAST() {
    }

    public static String imprimir(Programa programa) {
        StringBuilder sb = new StringBuilder("Programa\n");

        for (Directiva d : programa.directivas()) {
            sb.append("  Directiva ").append(d.nombre())
                    .append(" = \"").append(d.valor()).append("\"\n");
        }

        for (Agente a : programa.agentes()) {
            sb.append("  Agente ").append(a.nombre())
                    .append(" (línea ").append(a.fila()).append(")\n");
            sb.append("    Contexto: \"").append(a.contexto()).append("\"\n");

            for (Variable v : a.variables()) {
                sb.append("    Variable ").append(v.nombre())
                        .append(" = ").append(expresion(v.valor())).append('\n');
            }

            for (Comando c : a.comandos()) {
                sb.append("    Comando ").append(c.tipo()).append('\n');
                sb.append("      Argumento: ").append(expresion(c.argumento())).append('\n');
                for (Conector k : c.conectores()) {
                    sb.append("      ").append(k.tipo()).append(": ")
                            .append(expresion(k.argumento())).append('\n');
                }
                sb.append("      Destino: ").append(c.destino().nombre()).append('\n');
            }
        }

        for (Referencia e : programa.ejecuciones()) {
            sb.append("  Ejecutar ").append(e.nombre()).append('\n');
        }

        String exportados = programa.exportados().stream()
                .map(Referencia::nombre)
                .collect(Collectors.joining(", "));
        sb.append("  Exportar: ").append(exportados).append('\n');

        return sb.toString();
    }

    public static String expresion(Expresion e) {
        return e.terminos().stream()
                .map(ImpresorAST::termino)
                .collect(Collectors.joining(" + "));
    }

    private static String termino(Termino t) {
        return switch (t) {
            case TerminoCadena cad -> "\"" + cad.valor() + "\"";
            case TerminoId id -> id.nombre();
            case TerminoNumero num -> num.unidad() == null
                    ? num.valor()
                    : num.valor() + " " + num.unidad();
            case TerminoCarga carga -> "CARGAR(\"" + carga.archivo() + "\")";
        };
    }
}
