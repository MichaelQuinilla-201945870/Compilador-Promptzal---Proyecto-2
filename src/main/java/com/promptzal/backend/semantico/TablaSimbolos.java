package com.promptzal.backend.semantico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* Guarda los agentes (ámbito global) y las variables de cada agente.
    Una variable de un agente no es visible en otro.
 */
public class TablaSimbolos {

    public static final String AMBITO_GLOBAL = "global";

    private final List<Simbolo> simbolos = new ArrayList<>();                       // en orden de declaración
    private final Map<String, Simbolo> agentes = new LinkedHashMap<>();
    private final Map<String, Map<String, Simbolo>> variables = new LinkedHashMap<>(); // agente -> (nombre -> símbolo)

    // Devuelve false si ya existía un agente con ese nombre.
    public boolean declararAgente(String nombre, int fila, int columna) {
        if (agentes.containsKey(nombre)) {
            return false;
        }
        Simbolo s = new Simbolo(nombre, TipoSimbolo.AGENTE, AMBITO_GLOBAL, fila, columna);
        agentes.put(nombre, s);
        variables.put(nombre, new LinkedHashMap<>());
        simbolos.add(s);
        return true;
    }

    // Devuelve false si el nombre ya existe en ese ámbito (se conserva la primera declaración).
    public boolean declararVariable(String ambito, String nombre, TipoSimbolo tipo, int fila, int columna) {
        Map<String, Simbolo> locales = variables.get(ambito);
        if (locales == null || locales.containsKey(nombre)) {
            return false;
        }
        Simbolo s = new Simbolo(nombre, tipo, ambito, fila, columna);
        locales.put(nombre, s);
        simbolos.add(s);
        return true;
    }

    public boolean existeAgente(String nombre) {
        return agentes.containsKey(nombre);
    }

    public Simbolo buscarAgente(String nombre) {
        return agentes.get(nombre);
    }

    public boolean existeVariable(String ambito, String nombre) {
        Map<String, Simbolo> locales = variables.get(ambito);
        return locales != null && locales.containsKey(nombre);
    }

    // nombres de los agentes que declaran una variable con ese nombre.
    public List<String> agentesQueDeclaran(String nombre) {
        List<String> resultado = new ArrayList<>();
        for (Map.Entry<String, Map<String, Simbolo>> e : variables.entrySet()) {
            if (e.getValue().containsKey(nombre)) {
                resultado.add(e.getKey());
            }
        }
        return resultado;
    }

    public List<Simbolo> getSimbolos() {
        return Collections.unmodifiableList(simbolos);
    }
}