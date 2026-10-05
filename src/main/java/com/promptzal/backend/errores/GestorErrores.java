package com.promptzal.backend.errores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GestorErrores {

    private final List<ErrorCompilador> errores = new ArrayList<>();

    public void agregar(ErrorCompilador error) {
        errores.add(error);
    }

    public List<ErrorCompilador> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    public void limpiar() {
        errores.clear();
    }
}
