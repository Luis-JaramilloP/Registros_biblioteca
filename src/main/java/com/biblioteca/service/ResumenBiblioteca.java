package com.biblioteca.service;

import com.biblioteca.model.EstadoLectura;
import com.biblioteca.model.Genero;
import java.util.Map;

/**
 * Indicadores globales y estadísticas calculadas de la biblioteca.
 */
public record ResumenBiblioteca(
        int totalLibros,
        int totalSagas,
        int totalPaginasLeidas,
        double calificacionPromedio,
        Map<EstadoLectura, Long> librosPorEstado,
        Map<Genero, Long> librosPorGenero
) {}
