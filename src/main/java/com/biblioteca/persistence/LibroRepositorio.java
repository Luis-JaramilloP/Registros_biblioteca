package com.biblioteca.persistence;

import com.biblioteca.model.Libro;
import java.util.List;

/**
 * Contrato específico de persistencia para la entidad Libro.
 */
public interface LibroRepositorio extends Repositorio<Libro, String> {
    List<Libro> buscarPorSagaId(String sagaId);
}
