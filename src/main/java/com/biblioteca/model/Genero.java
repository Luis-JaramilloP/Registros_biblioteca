package com.biblioteca.model;

import java.io.Serializable;

/**
 * Categorías o géneros literarios soportados en la biblioteca.
 */
public enum Genero implements Serializable {
    FANTASIA("Fantasía"),
    CIENCIA_FICCION("Ciencia Ficción"),
    TERROR("Terror / Suspenso"),
    MISTERIO("Misterio / Policial"),
    HISTORICA("Novela Histórica"),
    ROMANCE("Romance"),
    AVENTURA("Aventura"),
    NO_FICCION("No Ficción / Ensayo"),
    POESIA_TEATRO("Poesía y Teatro"),
    OTRO("Otro");

    private final String descripcion;

    Genero(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
