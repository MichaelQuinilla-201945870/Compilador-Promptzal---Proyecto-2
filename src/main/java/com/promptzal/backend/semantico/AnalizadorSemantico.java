package com.promptzal.backend.semantico;

import com.promptzal.backend.ast.*;
import com.promptzal.backend.errores.ErrorCompilador;
import com.promptzal.backend.errores.GestorErrores;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Recorre el AST, llena la tabla de símbolos y reporta los errores semánticos.
public class AnalizadorSemantico {

    private final GestorErrores gestorErrores;
    private final TablaSimbolos tabla = new TablaSimbolos();

    public AnalizadorSemantico(GestorErrores gestorErrores) {
        this.gestorErrores = gestorErrores;
    }

    public TablaSimbolos analizar(Programa programa) {

        //Agentes: se registran y se analiza el cuerpo de cada uno
        for (Agente agente : programa.agentes()) {
            if (tabla.declararAgente(agente.nombre(), agente.fila(), agente.columna())) {
                analizarAgente(agente);
            } else {
                Simbolo original = tabla.buscarAgente(agente.nombre());
                error("El agente '" + agente.nombre() + "' ya fue declarado en la línea "
                        + original.fila(), agente.fila(), agente.columna());
            }
        }

        // EJECUTAR y EXPORTAR
        Set<String> ejecutados = validarEjecuciones(programa.ejecuciones());
        validarExportaciones(programa.exportados(), ejecutados);
        return tabla;
    }

    // Cuerpo de un agente
    private void analizarAgente(Agente agente) {
        String ambito = agente.nombre();

        for (Variable v : agente.variables()) {
            validarExpresion(v.valor(), ambito, false);       // primero se evalúa
            tabla.declararVariable(ambito, v.nombre(), TipoSimbolo.VARIABLE,
                    v.fila(), v.columna());                    //y luego se declara
        }

        for (Comando c : agente.comandos()) {
            // Excepción: en GENERAR el primer identificador es un tipo de contenido
            boolean omitirPrimero = c.tipo().equals("GENERAR");
            validarExpresion(c.argumento(), ambito, omitirPrimero);
            for (Conector k : c.conectores()) {
                validarExpresion(k.argumento(), ambito, false);
            }
            // El destino queda declarado aunque la expresión tuviera errores
            tabla.declararVariable(ambito, c.destino().nombre(), TipoSimbolo.RESULTADO,
                    c.destino().fila(), c.destino().columna());
        }
    }


    /* Revisa que cada identificador usado esté declarado en el ámbito.
      Las cadenas, números y CARGAR no se validan. La unidad de un número
      ("palabras" en "100 palabras") tampoco: no llega aquí como identificador.*/

    private void validarExpresion(Expresion expresion, String ambito, boolean omitirPrimero) {
        List<Termino> terminos = expresion.terminos();
        for (int i = 0; i < terminos.size(); i++) {
            if (i == 0 && omitirPrimero) {
                continue;
            }
            if (terminos.get(i) instanceof TerminoId id
                    && !tabla.existeVariable(ambito, id.nombre())) {
                error("La variable '" + id.nombre() + "' no está declarada antes de su uso en el agente '"
                        + ambito + "'", id.fila(), id.columna());
            }
        }
    }

    // EJECUTAR y EXPORTAR
    private Set<String> validarEjecuciones(List<Referencia> ejecuciones) {
        Set<String> ejecutados = new LinkedHashSet<>();
        for (Referencia r : ejecuciones) {
            if (tabla.existeAgente(r.nombre())) {
                ejecutados.add(r.nombre());
            } else {
                error("EJECUTAR hace referencia al agente '" + r.nombre() + "', que no existe",
                        r.fila(), r.columna());
            }
        }
        return ejecutados;
    }

    private void validarExportaciones(List<Referencia> exportados, Set<String> ejecutados) {
        for (Referencia r : exportados) {
            boolean existeEnEjecutado = ejecutados.stream()
                    .anyMatch(agente -> tabla.existeVariable(agente, r.nombre()));
            if (existeEnEjecutado) {
                continue;
            }
            List<String> duenios = tabla.agentesQueDeclaran(r.nombre());
            if (duenios.isEmpty()) {
                error("EXPORTAR hace referencia a '" + r.nombre()
                        + "', que no es una variable de ningún agente ejecutado", r.fila(), r.columna());
            } else {
                error("EXPORTAR hace referencia a '" + r.nombre() + "', que pertenece al agente '"
                        + duenios.get(0) + "', pero ese agente no se ejecuta", r.fila(), r.columna());
            }
        }
    }

    private void error(String descripcion, int fila, int columna) {
        gestorErrores.agregar(ErrorCompilador.semantico(descripcion, fila, columna));
    }
}