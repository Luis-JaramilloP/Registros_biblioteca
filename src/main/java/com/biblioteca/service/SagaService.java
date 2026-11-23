package com.biblioteca.service;

import com.biblioteca.model.Libro;
import com.biblioteca.model.Saga;
import com.biblioteca.persistence.LibroRepositorio;
import com.biblioteca.persistence.SagaRepositorio;

import java.util.*;

/**
 * Lógica de negocio para la administración de sagas literarias.
 */
public class SagaService {

    private final SagaRepositorio sagaRepositorio;
    private final LibroRepositorio libroRepositorio;

    public SagaService(SagaRepositorio sagaRepositorio, LibroRepositorio libroRepositorio) {
        this.sagaRepositorio = Objects.requireNonNull(sagaRepositorio, "El repositorio de sagas es requerido");
        this.libroRepositorio = Objects.requireNonNull(libroRepositorio, "El repositorio de libros es requerido");
    }

    public Saga crearSaga(String nombre, String autor, String descripcion, int totalVolumenes) {
        String id = UUID.randomUUID().toString();
        Saga saga = new Saga(id, nombre, autor, descripcion, totalVolumenes);
        sagaRepositorio.guardar(saga);
        return saga;
    }

    public void actualizarSaga(Saga saga) {
        Objects.requireNonNull(saga, "La saga no puede ser nula");
        if (!sagaRepositorio.existePorId(saga.id())) {
            throw new IllegalArgumentException("No existe ninguna saga con el identificador " + saga.id());
        }
        sagaRepositorio.guardar(saga);
    }

    public boolean eliminarSaga(String sagaId, boolean desvincularLibros) {
        if (sagaId == null || !sagaRepositorio.existePorId(sagaId)) {
            return false;
        }

        if (desvincularLibros) {
            // Desvincular libros asociados usando streams
            List<Libro> actualizados = libroRepositorio.listarTodos().stream()
                    .map(libro -> sagaId.equals(libro.sagaId())
                            ? new Libro(libro.id(), libro.titulo(), libro.autor(), libro.anio(),
                                        libro.paginas(), libro.genero(), null, null,
                                        libro.estadoLectura(), libro.calificacion(), libro.notas())
                            : libro)
                    .toList();
            libroRepositorio.guardarTodos(actualizados);
        } else {
            // Eliminar los libros asociados a la saga
            List<Libro> restantes = libroRepositorio.listarTodos().stream()
                    .filter(libro -> !sagaId.equals(libro.sagaId()))
                    .toList();
            libroRepositorio.guardarTodos(restantes);
        }

        return sagaRepositorio.eliminarPorId(sagaId);
    }

    public Optional<Saga> buscarPorId(String id) {
        return sagaRepositorio.buscarPorId(id);
    }

    public List<Saga> listarTodas() {
        return sagaRepositorio.listarTodos().stream()
                .sorted(Comparator.comparing(Saga::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Saga> buscar(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarTodas();
        }
        String filtro = termino.trim().toLowerCase();
        return sagaRepositorio.listarTodos().stream()
                .filter(s -> s.nombre().toLowerCase().contains(filtro)
                        || s.autor().toLowerCase().contains(filtro)
                        || s.descripcion().toLowerCase().contains(filtro))
                .sorted(Comparator.comparing(Saga::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public ProgresoSaga calcularProgreso(String sagaId) {
        Saga saga = sagaRepositorio.buscarPorId(sagaId)
                .orElseThrow(() -> new NoSuchElementException("Saga no encontrada: " + sagaId));

        List<Libro> libros = libroRepositorio.buscarPorSagaId(sagaId);
        int registrados = libros.size();
        long completados = libros.stream()
                .filter(l -> l.estadoLectura() == com.biblioteca.model.EstadoLectura.COMPLETADO)
                .count();

        double porcentaje = (saga.totalVolumenes() > 0)
                ? ((double) completados / saga.totalVolumenes()) * 100.0
                : 0.0;

        return new ProgresoSaga(saga, registrados, (int) completados, Math.min(100.0, porcentaje));
    }
}
