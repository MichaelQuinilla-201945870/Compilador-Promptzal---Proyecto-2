package com.promptzal.backend.afd;

import java.util.ArrayList;
import java.util.List;

// Un estado del automata y las transiciones que salen de el.
public final class EstadoAfd {

    // Una transicion: el simbolo que se lee y el estado al que lleva
    public record Arco(String simbolo, EstadoAfd destino) { }

    private final String nombre;
    private final List<Arco> arcos = new ArrayList<>();
    private String acepta;      // tipo de token que reconoce; null si no es estado de aceptacion
    private String clase;       // categoria de color del token (la misma del editor); puede ser null
    private String bucle;       // texto de la transicion que vuelve al mismo estado; null si no hay

    EstadoAfd(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }

    public List<Arco> arcos() {
        return arcos;
    }

    public String acepta() {
        return acepta;
    }

    public String clase() {
        return clase;
    }

    public String bucle() {
        return bucle;
    }

    void aceptar(String tipoToken, String clase) {
        this.acepta = tipoToken;
        this.clase = clase;
    }

    void setBucle(String bucle) {
        this.bucle = bucle;
    }

    EstadoAfd destinoPor(String simbolo) {
        for (Arco arco : arcos) {
            if (arco.simbolo().equals(simbolo)) {
                return arco.destino();
            }
        }
        return null;
    }
}