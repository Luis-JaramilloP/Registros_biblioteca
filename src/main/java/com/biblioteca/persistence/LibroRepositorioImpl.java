package com.biblioteca.persistence;

import com.biblioteca.model.Libro;
import java.nio.file.Path;
import java.util.List;

/**
 * Implementación de persistencia para Libros con almacenamiento serializado.
 */
public class LibroRepositorioImpl extends ArchivoRepositorio<Libro, String> implements LibroRepositorio {

    public LibroRepositorioImpl(Path archivo) {
        super(archivo, Libro::id);
    }

    @Override
    public List<Libro> buscarPorSagaId(String sagaId) {
        if (sagaId == null || sagaId.isBlank()) {
            return List.of();
        }
        return listarTodos().stream()
                .filter(l -> sagaId.equals(l.sagaId()))
                .toList();
    }
}
