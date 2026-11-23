package com.biblioteca.service;

import com.biblioteca.model.Saga;

/**
 * Resumen del avance de lectura de una saga calculada mediante Streams.
 */
public record ProgresoSaga(
        Saga saga,
        int librosRegistrados,
        int librosCompletados,
        double porcentajeCompletado
) {
    public String getEtiquetaProgreso() {
        return librosCompletados + " / " + saga.totalVolumenes() + " (" + String.format("%.1f", porcentajeCompletado) + "%)";
    }
}
