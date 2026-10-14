package com.promptzal.backend.ast;

import java.util.List;

public record Programa(List<Directiva> directivas, List<Agente> agentes,
                       List<Referencia> ejecuciones, List<Referencia> exportados) {
}
