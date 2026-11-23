package com.biblioteca.model;

import java.io.Serializable;

/**
 * Estados posibles del flujo de lectura de un libro.
 */
public enum EstadoLectura implements Serializable {
    PENDIENTE("Pendiente"),
    LEYENDO("Leyendo"),
    COMPLETADO("Completado"),
    ABANDONADO("Abandonado");

    private final String etiqueta;

    EstadoLectura(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
