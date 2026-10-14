package com.promptzal.backend.ast;

import java.util.List;
public record Comando(String tipo, Expresion argumento, List<Conector> conectores,
                      Referencia destino, int fila, int columna) {
}
