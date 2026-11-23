package com.biblioteca.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa una saga o serie literaria compuesta por múltiples volúmenes.
 * Implementada como Record inmutable de Java con validación de invariantes.
 *
 * @param id Identificador único de la saga
 * @param nombre Nombre oficial de la saga
 * @param autor Autor principal de la saga
 * @param descripcion Breve sinopsis o descripción temática
 * @param totalVolumenes Número total de volúmenes planeados o publicados (debe ser mayor a 0)
 */
public record Saga(
        String id,
        String nombre,
        String autor,
        String descripcion,
        int totalVolumenes
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor compacto para validar invariantes del record.
     */
    public Saga {
        Objects.requireNonNull(id, "El identificador no puede ser nulo");
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la saga no puede estar vacío");
        }
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("El autor de la saga no puede estar vacío");
        }
        if (totalVolumenes <= 0) {
            throw new IllegalArgumentException("El número de volúmenes debe ser mayor a 0");
        }
        descripcion = (descripcion == null) ? "" : descripcion.trim();
        nombre = nombre.trim();
        autor = autor.trim();
        id = id.trim();
    }

    /**
     * Genera una nueva instancia con un número actualizado de volúmenes totales.
     */
    public Saga conTotalVolumenes(int nuevoTotal) {
        return new Saga(this.id, this.nombre, this.autor, this.descripcion, nuevoTotal);
    }

    @Override
    public String toString() {
        return nombre + " (" + autor + ")";
    }
}
