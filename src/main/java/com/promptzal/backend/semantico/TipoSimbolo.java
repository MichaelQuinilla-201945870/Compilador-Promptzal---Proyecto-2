package com.promptzal.backend.semantico;

public enum TipoSimbolo {
    AGENTE("Agente"),
    VARIABLE("Variable declarada"),
    RESULTADO("Resultado de comando");

    private final String etiqueta;

    TipoSimbolo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}