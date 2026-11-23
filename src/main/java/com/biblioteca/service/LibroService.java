package com.biblioteca.service;

import com.biblioteca.model.EstadoLectura;
import com.biblioteca.model.Genero;
import com.biblioteca.model.Libro;
import com.biblioteca.persistence.LibroRepositorio;
import com.biblioteca.persistence.SagaRepositorio;

import java.util.*;
import java.util.stream.Stream;

/**
 * Lógica de negocio y consultas avanzadas con Java Streams para la entidad Libro.
 */
public class LibroService {

    private final LibroRepositorio libroRepositorio;
    private final SagaRepositorio sagaRepositorio;

    public LibroService(LibroRepositorio libroRepositorio, SagaRepositorio sagaRepositorio) {
        this.libroRepositorio = Objects.requireNonNull(libroRepositorio, "El repositorio de libros es requerido");
        this.sagaRepositorio = Objects.requireNonNull(sagaRepositorio, "El repositorio de sagas es requerido");
    }

    public Libro crearLibro(String titulo, String autor, int anio, int paginas,
                            Genero genero, String sagaId, Integer numeroEnSaga,
                            EstadoLectura estado, int calificacion, String notas) {
        validarPertenenciaSaga(sagaId, numeroEnSaga);
        validarDuplicado(null, titulo, autor);

        String id = UUID.randomUUID().toString();
        Libro libro = new Libro(id, titulo, autor, anio, paginas, genero,
                                sagaId, numeroEnSaga, estado, calificacion, notas);
        libroRepositorio.guardar(libro);
        return libro;
    }

    public void actualizarLibro(Libro libro) {
        Objects.requireNonNull(libro, "El libro no puede ser nulo");
        if (!libroRepositorio.existePorId(libro.id())) {
            throw new IllegalArgumentException("No existe ningún libro con el id: " + libro.id());
        }
        validarPertenenciaSaga(libro.sagaId(), libro.numeroEnSaga());
        validarDuplicado(libro.id(), libro.titulo(), libro.autor());

        libroRepositorio.guardar(libro);
    }

    public boolean eliminarLibro(String libroId) {
        return libroRepositorio.eliminarPorId(libroId);
    }

    public Optional<Libro> buscarPorId(String id) {
        return libroRepositorio.buscarPorId(id);
    }

    public List<Libro> listarTodos() {
        return libroRepositorio.listarTodos().stream()
                .sorted(Comparator.comparing(Libro::titulo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Búsqueda y filtrado multicriterio dinámico con Java Streams API.
     */
    public List<Libro> filtrar(String texto, Genero genero, EstadoLectura estado, String sagaId) {
        Stream<Libro> stream = libroRepositorio.listarTodos().stream();

        if (texto != null && !texto.isBlank()) {
            String query = texto.trim().toLowerCase();
            stream = stream.filter(l -> l.titulo().toLowerCase().contains(query)
                    || l.autor().toLowerCase().contains(query)
                    || l.notas().toLowerCase().contains(query));
        }

        if (genero != null) {
            stream = stream.filter(l -> l.genero() == genero);
        }

        if (estado != null) {
            stream = stream.filter(l -> l.estadoLectura() == estado);
        }

        if (sagaId != null && !sagaId.isBlank()) {
            if ("__INDEPENDIENTES__".equals(sagaId)) {
                stream = stream.filter(l -> !l.perteneceASaga());
            } else {
                stream = stream.filter(l -> sagaId.equals(l.sagaId()));
            }
        }

        return stream
                .sorted(Comparator.comparing(Libro::titulo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Obtiene los libros pertenecientes a una saga ordenados por su número de volumen.
     */
    public List<Libro> obtenerLibrosDeSaga(String sagaId) {
        if (sagaId == null || sagaId.isBlank()) return List.of();
        return libroRepositorio.listarTodos().stream()
                .filter(l -> sagaId.equals(l.sagaId()))
                .sorted(Comparator.comparing(
                        Libro::numeroEnSaga,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();
    }

    private void validarPertenenciaSaga(String sagaId, Integer numeroEnSaga) {
        if (sagaId != null && !sagaId.isBlank()) {
            if (!sagaRepositorio.existePorId(sagaId)) {
                throw new IllegalArgumentException("La saga referenciada no existe");
            }
            if (numeroEnSaga == null || numeroEnSaga <= 0) {
                throw new IllegalArgumentException("Si el libro pertenece a una saga, debe indicar un número de volumen válido");
            }
        }
    }

    private void validarDuplicado(String idActual, String titulo, String autor) {
        boolean duplicado = libroRepositorio.listarTodos().stream()
                .filter(l -> idActual == null || !l.id().equals(idActual))
                .anyMatch(l -> l.titulo().equalsIgnoreCase(titulo.trim())
                        && l.autor().equalsIgnoreCase(autor.trim()));

        if (duplicado) {
            throw new IllegalArgumentException("Ya existe un libro registrado con el título '" + titulo + "' del autor '" + autor + "'");
        }
    }
}
