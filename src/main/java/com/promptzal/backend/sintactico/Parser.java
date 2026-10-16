package com.promptzal.backend.sintactico;

import com.promptzal.backend.ast.*;
import com.promptzal.backend.errores.ErrorCompilador;
import com.promptzal.backend.errores.GestorErrores;
import com.promptzal.backend.lexico.TipoToken;
import com.promptzal.backend.lexico.Token;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Parser {

    // tokens de sincronización, según el contexto donde ocurrió el error

    private static final Set<TipoToken> SYNC_DIRECTIVAS = EnumSet.of(
            TipoToken.DIRECTIVA, TipoToken.AGENTE, TipoToken.EJECUTAR, TipoToken.EXPORTAR);

    private static final Set<TipoToken> SYNC_CUERPO = EnumSet.of(
            TipoToken.AGENTE, TipoToken.LLAVE_C, TipoToken.COMANDO_IA,
            TipoToken.VARIABLE, TipoToken.EJECUTAR, TipoToken.EXPORTAR);

    private static final Set<TipoToken> SYNC_AGENTE = EnumSet.of(
            TipoToken.AGENTE, TipoToken.EJECUTAR, TipoToken.EXPORTAR);

    private static final Set<TipoToken> SYNC_EJECUCIONES = EnumSet.of(
            TipoToken.EJECUTAR, TipoToken.EXPORTAR);

    private static final Set<TipoToken> SYNC_EXPORTAR = EnumSet.noneOf(TipoToken.class);

    private final List<Token> tokens;
    private final GestorErrores gestorErrores;
    private int posicion = 0;
    private Token actual;                    // token que se está mirando (vista previa de 1)
    private int posicionUltimoError = -1;    // evita reportar dos veces el mismo token
    private int erroresSintacticos = 0;

    public Parser(List<Token> tokens, GestorErrores gestorErrores) {
        this.tokens = tokens;
        this.gestorErrores = gestorErrores;
        this.actual = tokens.get(0);
    }

    // Devuelve el AST (parcial si hubo errores). Los errores quedan en el gestor.
    public Programa analizar() {
        try {
            return programa();
        } catch (ErrorSintacticoException e) {   // red de seguridad
            registrar(e);
            return null;
        }
    }

    public boolean huboErrores() {
        return erroresSintacticos > 0;
    }

    // Utilidades
    private boolean es(TipoToken tipo) {
        return actual.getTipo() == tipo;
    }

    private void avanzar() {
        if (actual.getTipo() != TipoToken.EOF) {   // EOF nunca se pasa de largo
            posicion++;
            actual = tokens.get(posicion);
        }
    }

    // Si el token actual es el esperado lo consume y lo devuelve; si no, error.
    private Token consumir(TipoToken esperado) {
        if (!es(esperado)) {
            throw error(nombreLegible(esperado));
        }
        Token consumido = actual;
        avanzar();
        return consumido;
    }

    private ErrorSintacticoException error(String esperado) {
        ErrorCompilador e = ErrorCompilador.sintactico(
                describir(actual), esperado, actual.getFila(), actual.getColumna());
        return new ErrorSintacticoException(e);
    }

    private static String describir(Token t) {
        if (t.getTipo() == TipoToken.EOF) {
            return "fin de archivo";
        }
        return "'" + t.getLexema() + "' (" + t.getTipo() + ")";
    }

    private static String nombreLegible(TipoToken t) {
        return t == TipoToken.EOF ? "fin de archivo" : t.name();
    }

    //Auxiliar (no es un no terminal): consume un ID y lo guarda como Referencia.
    private Referencia consumirReferencia() {
        Token id = consumir(TipoToken.ID);
        return new Referencia(id.getLexema(), id.getFila(), id.getColumna());
    }

    // Recuperación de errores

    // Reporta el error, salvo que ya se haya reportado uno en este mismo token.
    private void registrar(ErrorSintacticoException e) {
        if (posicion == posicionUltimoError) {
            return;
        }
        posicionUltimoError = posicion;
        erroresSintacticos++;
        gestorErrores.agregar(e.getError());
    }

    // Modo pánico: reporta y descarta tokens hasta uno de sincronización (o EOF).
    private void recuperar(ErrorSintacticoException e, Set<TipoToken> sincronizacion) {
        registrar(e);
        while (!es(TipoToken.EOF) && !sincronizacion.contains(actual.getTipo())) {
            avanzar();
        }
    }

    // 1. <programa> -> <directivas> <agentes> <ejecuciones> <exportar> EOF
    private Programa programa() {
        List<Directiva> directivas = directivas();
        List<Agente> agentes = agentes();
        List<Referencia> ejecuciones = ejecuciones();
        List<Referencia> exportados = exportar();
        try {
            consumir(TipoToken.EOF);
        } catch (ErrorSintacticoException e) {
            registrar(e);
        }
        return new Programa(directivas, agentes, ejecuciones, exportados);
    }

    // 2. <directivas> -> DIRECTIVA <cadena> <directivas> | ε
    private List<Directiva> directivas() {
        List<Directiva> lista = new ArrayList<>();
        while (es(TipoToken.DIRECTIVA)) {
            try {
                Token d = consumir(TipoToken.DIRECTIVA);
                String valor = cadena();
                lista.add(new Directiva(d.getLexema(), valor, d.getFila(), d.getColumna()));
            } catch (ErrorSintacticoException e) {
                recuperar(e, SYNC_DIRECTIVAS);
            }
        }
        return lista;
    }

    // 3. <agentes> -> <agente> <mas_agentes>
    private List<Agente> agentes() {
        List<Agente> lista = new ArrayList<>();
        intentarAgente(lista);
        masAgentes(lista);
        return lista;
    }

    // 4. <mas_agentes> -> <agente> <mas_agentes> | ε
    private void masAgentes(List<Agente> lista) {
        while (es(TipoToken.AGENTE)) {
            intentarAgente(lista);
        }
    }

    //Punto de recuperación: un agente dañado en su cabecera se descarta completo.
    private void intentarAgente(List<Agente> lista) {
        try {
            lista.add(agente());
        } catch (ErrorSintacticoException e) {
            recuperar(e, SYNC_AGENTE);
        }
    }

    // 5. <agente> -> AGENTE ID LLAVE_A <contexto> <variables> <comandos> LLAVE_C
    private Agente agente() {
        consumir(TipoToken.AGENTE);
        Token nombre = consumir(TipoToken.ID);
        consumir(TipoToken.LLAVE_A);

        String contexto = "";
        try {
            contexto = contexto();
        } catch (ErrorSintacticoException e) {
            recuperar(e, SYNC_CUERPO);
        }

        List<Variable> variables = variables();
        List<Comando> comandos = comandos();

        try {
            consumir(TipoToken.LLAVE_C);
        } catch (ErrorSintacticoException e) {
            registrar(e);       // falta '}': se reporta, pero se conserva el agente
        }
        return new Agente(nombre.getLexema(), contexto, variables, comandos,
                nombre.getFila(), nombre.getColumna());
    }

    // 6. <contexto> -> CONTEXTO IGUAL <cadena>
    private String contexto() {
        consumir(TipoToken.CONTEXTO);
        consumir(TipoToken.IGUAL);
        return cadena();
    }

    // 7. <variables> -> VARIABLE ID IGUAL <expresion> <variables> | ε
    private List<Variable> variables() {
        List<Variable> lista = new ArrayList<>();
        while (es(TipoToken.VARIABLE)) {
            try {
                consumir(TipoToken.VARIABLE);
                Token id = consumir(TipoToken.ID);
                consumir(TipoToken.IGUAL);
                Expresion valor = expresion();
                lista.add(new Variable(id.getLexema(), valor, id.getFila(), id.getColumna()));
            } catch (ErrorSintacticoException e) {
                recuperar(e, SYNC_CUERPO);
            }
        }
        return lista;
    }

    // 8. <comandos> -> <comando> <mas_comandos>
    private List<Comando> comandos() {
        List<Comando> lista = new ArrayList<>();
        intentarComando(lista);
        masComandos(lista);
        return lista;
    }

    // 9. <mas_comandos> -> <comando> <mas_comandos> | ε
    private void masComandos(List<Comando> lista) {
        while (es(TipoToken.COMANDO_IA)) {
            intentarComando(lista);
        }
    }

    //Punto de recuperación: un comando dañado se pierde, los demás siguen.
    private void intentarComando(List<Comando> lista) {
        try {
            lista.add(comando());
        } catch (ErrorSintacticoException e) {
            recuperar(e, SYNC_CUERPO);
        }
    }

    // 10. <comando> -> COMANDO_IA <expresion> <conectores> FLECHA ID
    private Comando comando() {
        Token cmd = consumir(TipoToken.COMANDO_IA);
        Expresion argumento = expresion();
        List<Conector> conectores = conectores();
        consumir(TipoToken.FLECHA);
        Referencia destino = consumirReferencia();
        return new Comando(cmd.getLexema(), argumento, conectores, destino,
                cmd.getFila(), cmd.getColumna());
    }

    // 11. <conectores> -> CONECTOR <expresion> <conectores> | ε
    private List<Conector> conectores() {
        List<Conector> lista = new ArrayList<>();
        while (es(TipoToken.CONECTOR)) {
            Token c = consumir(TipoToken.CONECTOR);
            Expresion argumento = expresion();
            lista.add(new Conector(c.getLexema(), argumento, c.getFila(), c.getColumna()));
        }
        return lista;
    }

    // 12. <expresion> -> <termino> <mas_terminos>
    private Expresion expresion() {
        List<Termino> lista = new ArrayList<>();
        lista.add(termino());
        masTerminos(lista);
        return new Expresion(lista);
    }

    // 13. <mas_terminos> -> MAS <termino> <mas_terminos> | ε
    private void masTerminos(List<Termino> lista) {
        while (es(TipoToken.MAS)) {
            consumir(TipoToken.MAS);
            lista.add(termino());
        }
    }

    // 14. <termino> -> <cadena> | ID | NUMERO <unidad> | <cargar>
    private Termino termino() {
        switch (actual.getTipo()) {
            case COMILLA -> {
                Token inicio = actual;
                String valor = cadena();
                return new TerminoCadena(valor, inicio.getFila(), inicio.getColumna());
            }
            case ID -> {
                Token id = consumir(TipoToken.ID);
                return new TerminoId(id.getLexema(), id.getFila(), id.getColumna());
            }
            case NUMERO -> {
                Token num = consumir(TipoToken.NUMERO);
                String unidad = unidad();
                return new TerminoNumero(num.getLexema(), unidad, num.getFila(), num.getColumna());
            }
            case CARGAR -> {
                return cargar();
            }
            default -> throw error("COMILLA, ID, NUMERO o CARGAR");
        }
    }

    // 15. <unidad> -> ID | ε
    private String unidad() {
        if (es(TipoToken.ID)) {
            return consumir(TipoToken.ID).getLexema();
        }
        return null;                // ε: el número no tiene unidad
    }

    // 16. <cargar> -> CARGAR PAR_A <cadena> PAR_C
    private TerminoCarga cargar() {
        Token c = consumir(TipoToken.CARGAR);
        consumir(TipoToken.PAR_A);
        String archivo = cadena();
        consumir(TipoToken.PAR_C);
        return new TerminoCarga(archivo, c.getFila(), c.getColumna());
    }

    // 17. <ejecuciones> -> EJECUTAR ID <mas_ejecuciones>   (la primera es obligatoria)
    private List<Referencia> ejecuciones() {
        List<Referencia> lista = new ArrayList<>();
        intentarEjecucion(lista);
        masEjecuciones(lista);
        return lista;
    }

    // 18. <mas_ejecuciones> -> EJECUTAR ID <mas_ejecuciones> | ε
    private void masEjecuciones(List<Referencia> lista) {
        while (es(TipoToken.EJECUTAR)) {
            intentarEjecucion(lista);
        }
    }

    private void intentarEjecucion(List<Referencia> lista) {
        try {
            consumir(TipoToken.EJECUTAR);
            lista.add(consumirReferencia());
        } catch (ErrorSintacticoException e) {
            recuperar(e, SYNC_EJECUCIONES);
        }
    }

    // 19. <exportar> -> EXPORTAR ID <mas_ids>
    private List<Referencia> exportar() {
        List<Referencia> lista = new ArrayList<>();
        try {
            consumir(TipoToken.EXPORTAR);
            lista.add(consumirReferencia());
            masIds(lista);
        } catch (ErrorSintacticoException e) {
            recuperar(e, SYNC_EXPORTAR);     // no hay nada válido después: hasta EOF
        }
        return lista;
    }

    // 20. <mas_ids> -> COMA ID <mas_ids> | ε
    private void masIds(List<Referencia> lista) {
        while (es(TipoToken.COMA)) {
            consumir(TipoToken.COMA);
            lista.add(consumirReferencia());
        }
    }

    // 21. <cadena> -> COMILLA <contenido_cadena> COMILLA
    private String cadena() {
        consumir(TipoToken.COMILLA);
        String contenido = contenidoCadena();
        consumir(TipoToken.COMILLA);
        return contenido;
    }

    // 22. <contenido_cadena> -> CADENA | ε
    private String contenidoCadena() {
        if (es(TipoToken.CADENA)) {
            return consumir(TipoToken.CADENA).getLexema();
        }
        return "";                  // ε: la cadena vacía ""
    }
}