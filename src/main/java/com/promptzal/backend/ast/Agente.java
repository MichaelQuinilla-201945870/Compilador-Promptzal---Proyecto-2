package com.promptzal.backend.ast;

import java.util.List;

public record Agente(String nombre, String contexto, List<Variable> variables,
                     List<Comando> comandos, int fila, int columna) {
}
