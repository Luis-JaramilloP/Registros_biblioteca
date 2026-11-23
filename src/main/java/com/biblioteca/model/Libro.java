package com.biblioteca.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa un libro en la biblioteca personal.
 * Implementado como Record inmutable de Java con validación en su constructor compacto.
 *
 * @param id Identificador único del libro
 * @param titulo Título de la obra
 * @param autor Autor del libro
 * @param anio Año de publicación
 * @param paginas Número de páginas (mayor o igual a 0)
 * @param genero Género literario
 * @param sagaId Identificador de la saga a la que pertenece (puede ser null o vacío si es independiente)
 * @param numeroEnSaga Número de volumen dentro de la saga (opcional, null si no aplica)
 * @param estadoLectura Estado actual del libro en el ciclo de lectura
 * @param calificacion Calificación personal de 0 a 5 estrellas
 * @param notas Notas, reflexiones o reseña breve
 */
public record Libro(
        String id,
        String titulo,
        String autor,
        int anio,
        int paginas,
        Genero genero,
        String sagaId,
        Integer numeroEnSaga,
        EstadoLectura estadoLectura,
        int calificacion,
        String notas
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor compacto para garantizar invariantes.
     */
    public Libro {
        Objects.requireNonNull(id, "El ID del libro no puede ser nulo");
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título del libro no puede estar vacío");
        }
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("El autor del libro no puede estar vacío");
        }
        if (paginas < 0) {
            throw new IllegalArgumentException("El número de páginas no puede ser negativo");
        }
        if (calificacion < 0 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe estar comprendida entre 0 y 5");
        }
        genero = (genero == null) ? Genero.OTRO : genero;
        estadoLectura = (estadoLectura == null) ? EstadoLectura.PENDIENTE : estadoLectura;
        sagaId = (sagaId == null || sagaId.trim().isEmpty()) ? null : sagaId.trim();
        notas = (notas == null) ? "" : notas.trim();
        titulo = titulo.trim();
        autor = autor.trim();
        id = id.trim();

        if (sagaId != null && numeroEnSaga != null && numeroEnSaga <= 0) {
            throw new IllegalArgumentException("El número de volumen en la saga debe ser mayor a 0");
        }
    }

    /**
     * Indica si el libro pertenece a una saga.
     */
    public boolean perteneceASaga() {
        return sagaId != null && !sagaId.isBlank();
    }

    /**
     * Retorna una copia con un nuevo estado de lectura.
     */
    public Libro conEstadoLectura(EstadoLectura nuevoEstado) {
        return new Libro(this.id, this.titulo, this.autor, this.anio, this.paginas,
                this.genero, this.sagaId, this.numeroEnSaga, nuevoEstado, this.calificacion, this.notas);
    }

    /**
     * Retorna una copia con una calificación actualizada.
     */
    public Libro conCalificacion(int nuevaCalificacion) {
        return new Libro(this.id, this.titulo, this.autor, this.anio, this.paginas,
                this.genero, this.sagaId, this.numeroEnSaga, this.estadoLectura, nuevaCalificacion, this.notas);
    }
}
