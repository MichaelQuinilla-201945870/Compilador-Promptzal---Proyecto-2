package com.promptzal.backend.lexico;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


// Vocabulario de PromptZal

public final class PalabrasClave {

        private static final Map<String, TipoToken> PALABRAS = new HashMap<>();
        private static final Set<String> DIRECTIVAS = Set.of("modelo", "rol", "formato");

        static {
            // Estructura
            PALABRAS.put("AGENTE", TipoToken.AGENTE);
            PALABRAS.put("contexto", TipoToken.CONTEXTO);
            PALABRAS.put("variable", TipoToken.VARIABLE);
            PALABRAS.put("EJECUTAR", TipoToken.EJECUTAR);
            PALABRAS.put("EXPORTAR", TipoToken.EXPORTAR);

            // Función del sistema
            PALABRAS.put("CARGAR", TipoToken.CARGAR);

            // Comandos de IA
            for (String comando : new String[]{
                    "PREGUNTAR", "GENERAR", "RESUMIR", "ANALIZAR",
                    "TRADUCIR", "CLASIFICAR", "EXTRAER", "CODIFICAR"}) {
                PALABRAS.put(comando, TipoToken.COMANDO_IA);
            }

            // Conectores
            for (String conector : new String[]{"SOBRE", "DESDE", "EN", "COMO"}) {
                PALABRAS.put(conector, TipoToken.CONECTOR);
            }
        }

        private PalabrasClave() {
        }

        // Devuelve el tipo de la palabra, o ID si no es palabra del lenguaje.
        public static TipoToken tipoDePalabra(String palabra) {
            return PALABRAS.getOrDefault(palabra, TipoToken.ID);
        }

        //Recibe el nombre SIN la arroba: "modelo", "rol", "formato".
        public static boolean esDirectivaValida(String nombre) {
            return DIRECTIVAS.contains(nombre);
        }

}
