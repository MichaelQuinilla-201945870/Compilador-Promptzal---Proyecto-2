package com.promptzal.backend.afd;

import com.promptzal.backend.lexico.PalabrasClave;
import com.promptzal.backend.lexico.TipoToken;
import com.promptzal.backend.lexico.Token;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/* El autómata finito determinista del lenguaje, construido a partir del vocabulario real
  (PalabrasClave). Cada palabra tiene su propio camino de estados y su propio estado de
  aceptacion; el unico estado general es el de los identificadores. */
public final class Afd {

    private final EstadoAfd inicial = new EstadoAfd("q0");
    private final List<EstadoAfd> estados = new ArrayList<>(List.of(inicial));
    private final Map<String, EstadoAfd> porLexema = new HashMap<>();   // "AGENTE", "@rol", "->", ...

    private EstadoAfd identificador;
    private EstadoAfd entero;
    private EstadoAfd decimal;
    private EstadoAfd comillaApertura;
    private EstadoAfd comillaCierre;
    private EstadoAfd cadena;

    private Afd() {
    }

    public static Afd construir() {
        Afd afd = new Afd();
        afd.armar();
        return afd;
    }

    public EstadoAfd inicial() {
        return inicial;
    }

    public List<EstadoAfd> estados() {
        return estados;
    }

    public long cantidadDeAceptacion() {
        return estados.stream().filter(e -> e.acepta() != null).count();
    }

    // Estados de aceptacion
    public Set<EstadoAfd> alcanzados(List<Token> tokens) {
        Set<EstadoAfd> alcanzados = new HashSet<>();
        for (Token token : tokens) {
            switch (token.getTipo()) {
                case EOF -> { }
                case ID -> alcanzados.add(identificador);
                case NUMERO -> alcanzados.add(token.getLexema().contains(".") ? decimal : entero);
                case COMILLA -> {
                    alcanzados.add(comillaApertura);
                    alcanzados.add(comillaCierre);
                }
                case CADENA -> alcanzados.add(cadena);
                default -> {
                    EstadoAfd estado = porLexema.get(token.getLexema());
                    if (estado != null) {
                        alcanzados.add(estado);
                    }
                }
            }
        }
        return alcanzados;
    }

    // Construccion
    private void armar() {
        inicial.setBucle("espacio, tabulación o salto de línea");

        // Directivas: q0 --@--> q@ y luego un camino por cada nombre (@modelo, @rol, @formato)
        EstadoAfd arroba = agregar(inicial, "@", "q@", null, null);
        for (String nombre : new TreeSet<>(PalabrasClave.directivas())) {
            insertar(arroba, "@", nombre, TipoToken.DIRECTIVA);
        }

        // Palabras reservadas, comandos, funcion y conectores: un camino por palabra
        new TreeMap<>(PalabrasClave.palabras()).forEach((palabra, tipo) -> insertar(inicial, "", palabra, tipo));

        // El unico estado general: identificadores
        identificador = agregar(inicial, "letra o _", "qID", TipoToken.ID, null);
        identificador.setBucle("letra, dígito o _");

        // Numeros
        entero = agregar(inicial, "dígito", "qENT", TipoToken.NUMERO, null);
        entero.setBucle("dígito");
        EstadoAfd punto = agregar(entero, ".", "qPUNTO", null, null);
        decimal = agregar(punto, "dígito", "qDEC", TipoToken.NUMERO, null);
        decimal.setBucle("dígito");

        // Cadenas: la comilla de apertura, el contenido y la comilla de cierre
        comillaApertura = agregar(inicial, "\"", "qCOMILLA_1", TipoToken.COMILLA, null);
        cadena = agregar(comillaApertura, "cualquiera", "qCADENA", TipoToken.CADENA, null);
        cadena.setBucle("cualquier carácter menos comilla y salto de línea");
        comillaCierre = agregar(cadena, "\"", "qCOMILLA_2", TipoToken.COMILLA, null);

        // Operadores y delimitadores
        agregar(inicial, "=", "qIGUAL", TipoToken.IGUAL, "=");
        agregar(inicial, "+", "qMAS", TipoToken.MAS, "+");
        agregar(inicial, "{", "qLLAVE_A", TipoToken.LLAVE_A, "{");
        agregar(inicial, "}", "qLLAVE_C", TipoToken.LLAVE_C, "}");
        agregar(inicial, "(", "qPAR_A", TipoToken.PAR_A, "(");
        agregar(inicial, ")", "qPAR_C", TipoToken.PAR_C, ")");
        agregar(inicial, ",", "qCOMA", TipoToken.COMA, ",");
        EstadoAfd guion = agregar(inicial, "-", "qGUION", null, null);
        agregar(guion, ">", "qFLECHA", TipoToken.FLECHA, "->");

        // Comentarios: no producen token; al terminar se vuelve a q0
        EstadoAfd barra = agregar(inicial, "/", "qBARRA", null, null);
        EstadoAfd linea = agregar(barra, "/", "qCOMENTARIO_LINEA", null, null);
        linea.setBucle("cualquier carácter menos salto de línea");
        EstadoAfd bloque = agregar(barra, "*", "qCOMENTARIO_BLOQUE", null, null);
        bloque.setBucle("cualquier carácter menos *");
        EstadoAfd asterisco = agregar(bloque, "*", "qASTERISCO", null, null);
        asterisco.setBucle("*");
    }

    //  Agrega el camino de una palabra, letra por letra

    private void insertar(EstadoAfd desde, String prefijo, String palabra, TipoToken tipo) {
        EstadoAfd actual = desde;
        StringBuilder nombre = new StringBuilder("q").append(prefijo);
        for (char letra : palabra.toCharArray()) {
            nombre.append(letra);
            String simbolo = String.valueOf(letra);
            EstadoAfd siguiente = actual.destinoPor(simbolo);
            if (siguiente == null) {
                siguiente = agregar(actual, simbolo, nombre.toString(), null, null);
            }
            actual = siguiente;
        }
        actual.aceptar(tipo.name(), clase(tipo));
        porLexema.put(prefijo + palabra, actual);
    }

    // Crea un estado nuevo y lo conecta desde otro con la transicion indicada
    private EstadoAfd agregar(EstadoAfd desde, String simbolo, String nombre, TipoToken acepta, String lexema) {
        EstadoAfd nuevo = new EstadoAfd(nombre);
        if (acepta != null) {
            nuevo.aceptar(acepta.name(), clase(acepta));
        }
        desde.arcos().add(new EstadoAfd.Arco(simbolo, nuevo));
        estados.add(nuevo);
        if (lexema != null) {
            porLexema.put(lexema, nuevo);
        }
        return nuevo;
    }

    // Misma categoria de color que usa el editor para cada tipo de token. */
    private static String clase(TipoToken tipo) {
        return switch (tipo) {
            case DIRECTIVA -> "pz-directiva";
            case AGENTE, CONTEXTO, VARIABLE, EJECUTAR, EXPORTAR -> "pz-reservada";
            case COMANDO_IA, CARGAR -> "pz-comando";
            case CONECTOR, FLECHA -> "pz-conector";
            case COMILLA, CADENA -> "pz-cadena";
            case NUMERO -> "pz-numero";
            default -> null;
        };
    }
}