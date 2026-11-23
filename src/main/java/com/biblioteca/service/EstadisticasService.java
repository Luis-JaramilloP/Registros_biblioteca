package com.biblioteca.service;

import com.biblioteca.model.EstadoLectura;
import com.biblioteca.model.Genero;
import com.biblioteca.model.Libro;
import com.biblioteca.persistence.LibroRepositorio;
import com.biblioteca.persistence.SagaRepositorio;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio encargado de computar métricas y análisis de la biblioteca
 * mediante el uso intensivo de la API de Streams y funciones de reducción.
 */
public class EstadisticasService {

    private final LibroRepositorio libroRepositorio;
    private final SagaRepositorio sagaRepositorio;

    public EstadisticasService(LibroRepositorio libroRepositorio, SagaRepositorio sagaRepositorio) {
        this.libroRepositorio = Objects.requireNonNull(libroRepositorio, "El repositorio de libros es requerido");
        this.sagaRepositorio = Objects.requireNonNull(sagaRepositorio, "El repositorio de sagas es requerido");
    }

    /**
     * Genera un resumen completo de métricas utilizando Streams de Java.
     */
    public ResumenBiblioteca generarResumen() {
        List<Libro> libros = libroRepositorio.listarTodos();
        int totalLibros = libros.size();
        int totalSagas = (int) sagaRepositorio.contar();

        // Páginas de libros terminados
        int paginasLeidas = libros.stream()
                .filter(l -> l.estadoLectura() == EstadoLectura.COMPLETADO)
                .mapToInt(Libro::paginas)
                .sum();

        // Calificación promedio (solo libros calificados con más de 0)
        double promedioCalificacion = libros.stream()
                .filter(l -> l.calificacion() > 0)
                .mapToInt(Libro::calificacion)
                .average()
                .orElse(0.0);

        // Agrupación por Estado de Lectura
        Map<EstadoLectura, Long> porEstado = libros.stream()
                .collect(Collectors.groupingBy(Libro::estadoLectura, Collectors.counting()));

        // Asegurar que todos los estados tengan un valor en el mapa
        for (EstadoLectura estado : EstadoLectura.values()) {
            porEstado.putIfAbsent(estado, 0L);
        }

        // Agrupación por Género
        Map<Genero, Long> porGenero = libros.stream()
                .collect(Collectors.groupingBy(Libro::genero, Collectors.counting()));

        return new ResumenBiblioteca(
                totalLibros,
                totalSagas,
                paginasLeidas,
                promedioCalificacion,
                porEstado,
                porGenero
        );
    }

    /**
     * Retorna los libros mejor calificados limitados al número indicado.
     */
    public List<Libro> obtenerTopCalificados(int limite) {
        return libroRepositorio.listarTodos().stream()
                .filter(l -> l.calificacion() > 0)
                .sorted(Comparator.comparingInt(Libro::calificacion).reversed()
                        .thenComparing(Libro::titulo))
                .limit(limite)
                .toList();
    }
}
