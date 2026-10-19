package com.promptzal.backend.semantico;

import com.promptzal.backend.ast.Referencia;
import java.util.ArrayList;
import java.util.List;

// Si hay otra ejecución en medio (a, b, a).
public final class PlanEjecucion {

    private PlanEjecucion() {
    }

    // true si la ejecucion en esa posicion repite la anterior.
    public static boolean esRepeticionInmediata(List<Referencia> ejecuciones, int indice) {
        return indice > 0
                && ejecuciones.get(indice).nombre().equals(ejecuciones.get(indice - 1).nombre());
    }

    // Las ejecuciones que cuentan, en orden.
    public static List<Referencia> depurar(List<Referencia> ejecuciones) {
        List<Referencia> plan = new ArrayList<>();
        for (int i = 0; i < ejecuciones.size(); i++) {
            if (!esRepeticionInmediata(ejecuciones, i)) {
                plan.add(ejecuciones.get(i));
            }
        }
        return plan;
    }
}