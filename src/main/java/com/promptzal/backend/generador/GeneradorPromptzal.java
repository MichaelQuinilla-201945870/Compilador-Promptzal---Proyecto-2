package com.promptzal.backend.generador;

import com.promptzal.backend.ast.*;
import com.promptzal.backend.semantico.PlanEjecucion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Recorre el AST y produce el promptzal
public class GeneradorPromptzal {

    public String generar(Programa programa) {
        StringBuilder sb = new StringBuilder("# Prompt generado con PromptZal\n");
        encabezado(sb, programa.directivas());

        // Si un agente está duplicado se usa el primero (el duplicado ya es error semantico)
        Map<String, Agente> agentes = new LinkedHashMap<>();
        for (Agente a : programa.agentes()) {
            agentes.putIfAbsent(a.nombre(), a);
        }

        List<Referencia> plan = PlanEjecucion.depurar(programa.ejecuciones());

        // Cuantas veces se ejecuta cada agente, para rotular "ejecución 1 de 2"
        Map<String, Integer> totales = new HashMap<>();
        for (Referencia r : plan) {
            if (agentes.containsKey(r.nombre())) {
                totales.merge(r.nombre(), 1, Integer::sum);
            }
        }

        Map<String, Integer> vistas = new HashMap<>();
        for (Referencia r : plan) {
            Agente agente = agentes.get(r.nombre());
            if (agente == null) {
                continue;                 // agente inexistente: ya es error semantico
            }
            int numero = vistas.merge(r.nombre(), 1, Integer::sum);
            seccionAgente(sb, agente, numero, totales.get(r.nombre()));
        }

        sb.append("\n## Resultado solicitado\n");
        for (Referencia e : programa.exportados()) {
            sb.append("- ").append(e.nombre()).append('\n');
        }
        return sb.toString();
    }


    // Encabezado y secciones
    private void encabezado(StringBuilder sb, List<Directiva> directivas) {
        Map<String, String> valores = new HashMap<>();
        for (Directiva d : directivas) {
            valores.put(d.nombre(), d.valor());       // si una directiva se repite, vale la ultima
        }
        linea(sb, "Modelo destino", valores.get("@modelo"));
        linea(sb, "Rol general", valores.get("@rol"));
        linea(sb, "Formato de respuesta", valores.get("@formato"));
    }

    private void linea(StringBuilder sb, String etiqueta, String valor) {
        if (valor != null) {                          // solo las directivas declaradas
            sb.append(etiqueta).append(": ").append(valor).append('\n');
        }
    }

    private void seccionAgente(StringBuilder sb, Agente agente, int numero, int total) {
        sb.append("\n## Agente: ").append(agente.nombre());
        if (total > 1) {
            sb.append(" (ejecución ").append(numero).append(" de ").append(total).append(')');
        }
        sb.append('\n');
        if (numero > 1) {
            sb.append("Nota: nueva ejecución de este agente; aplica de nuevo sus instrucciones "
                    + "sobre el estado actual de los resultados.\n");
        }
        sb.append("Contexto: ").append(agente.contexto()).append('\n');

        if (!agente.variables().isEmpty()) {
            sb.append("\nVariables disponibles:\n");
            for (Variable v : agente.variables()) {
                sb.append("- ").append(v.nombre()).append(": ").append(origen(v.valor())).append('\n');
            }
        }

        sb.append("\nInstrucciones:\n");
        int n = 1;
        for (Comando c : agente.comandos()) {
            sb.append(n++).append(". ").append(instruccion(c)).append('\n');
        }
    }

    // Un comando -> una instruccion en lenguaje natural
    private String instruccion(Comando c) {
        List<Conector> pendientes = new ArrayList<>(c.conectores());
        String arg = texto(c.argumento());

        // Cada comando toma de sus conectores lo que necesita; lo demás queda en "pendientes"
        String frase = switch (c.tipo()) {
            case "PREGUNTAR" -> {
                Expresion sobre = tomar(pendientes, "SOBRE");
                yield sobre != null
                        ? "Basándote en " + texto(sobre) + ", responde: " + arg
                        : "Responde: " + arg;
            }
            case "GENERAR" ->
                    "Genera " + arg + opcional(tomar(pendientes, "DESDE"), " a partir de: ");
            case "RESUMIR" -> {
                Expresion en = tomar(pendientes, "EN");
                yield "Resume " + arg + (en != null ? " en un máximo de " + limitePalabras(en) : "");
            }
            case "ANALIZAR" ->
                    "Analiza " + arg + opcional(tomar(pendientes, "SOBRE"), " considerando: ");
            case "TRADUCIR" ->
                    "Traduce " + arg + opcional(tomar(pendientes, "EN"), " al idioma ");
            case "CLASIFICAR" ->
                    "Clasifica " + arg + opcional(tomar(pendientes, "EN"), " en las categorías: ");
            case "EXTRAER" ->
                    "Extrae " + arg + opcional(tomar(pendientes, "DESDE"), " a partir de ");
            default ->      // comando agregado en PalabrasClave
                    "Realiza la acción " + c.tipo() + " sobre: " + arg;
        };

        StringBuilder sb = new StringBuilder(conPunto(frase));

        Expresion como = tomar(pendientes, "COMO");          // COMO aplica a cualquier comando
        for (Conector k : pendientes) {                       // conectores adicionales
            sb.append(' ').append(conPunto("Dato adicional (" + k.tipo() + "): " + texto(k.argumento())));
        }
        if (como != null) {
            sb.append(' ').append(conPunto("Entrega el resultado en formato " + texto(como)));
        }

        String guardado = c.tipo().equals("PREGUNTAR") ? "la respuesta" : "el resultado";
        sb.append(" Guarda ").append(guardado).append(" como ").append(c.destino().nombre()).append('.');
        return sb.toString();
    }

    // Saca de la lista el primer conector de ese tipo y devuelve su argumento (o null).
    private static Expresion tomar(List<Conector> conectores, String tipo) {
        for (int i = 0; i < conectores.size(); i++) {
            if (conectores.get(i).tipo().equals(tipo)) {
                return conectores.remove(i).argumento();
            }
        }
        return null;
    }

    private static String opcional(Expresion e, String prefijo) {
        return e == null ? "" : prefijo + texto(e);
    }

    // "100 palabras" se deja igual; un "100" suelto se completa con "palabras".
    private static String limitePalabras(Expresion en) {
        String t = texto(en);
        if (en.terminos().size() == 1
                && en.terminos().get(0) instanceof TerminoNumero n && n.unidad() == null) {
            t += " palabras";
        }
        return t;
    }

    // Agrega el punto final salvo que la frase ya termine en . ? o !
    private static String conPunto(String frase) {
        String t = frase.strip();
        char ultimo = t.charAt(t.length() - 1);
        return (ultimo == '.' || ultimo == '?' || ultimo == '!') ? t : t + ".";
    }


    // Expresiones -> texto

    // Para las instrucciones: cadenas sin comillas; "+" concatena.
    private static String texto(Expresion e) {
        StringBuilder sb = new StringBuilder();
        for (Termino t : e.terminos()) {
            String parte = switch (t) {
                case TerminoCadena c -> c.valor();
                case TerminoId id -> id.nombre();
                case TerminoNumero n -> numero(n);
                case TerminoCarga k -> "el contenido del archivo \"" + k.archivo() + "\"";
            };
            boolean hayEspacio = sb.isEmpty()
                    || Character.isWhitespace(sb.charAt(sb.length() - 1))
                    || parte.isEmpty()
                    || Character.isWhitespace(parte.charAt(0));
            if (!hayEspacio) {
                sb.append(' ');           // "detalle" + variable -> "detalle variable"
            }
            sb.append(parte);
        }
        return sb.toString().strip();
    }

    // Para "Variables disponibles": de dónde viene el valor.
    private static String origen(Expresion e) {
        List<String> partes = new ArrayList<>();
        for (Termino t : e.terminos()) {
            partes.add(switch (t) {
                case TerminoCadena c -> "\"" + c.valor() + "\"";
                case TerminoId id -> id.nombre();
                case TerminoNumero n -> numero(n);
                case TerminoCarga k ->
                        "contenido del archivo \"" + k.archivo() + "\" (adjuntar el archivo)";
            });
        }
        return String.join(" + ", partes);
    }

    private static String numero(TerminoNumero n) {
        return n.unidad() == null ? n.valor() : n.valor() + " " + n.unidad();
    }
}