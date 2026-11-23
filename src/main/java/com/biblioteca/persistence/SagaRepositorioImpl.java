package com.biblioteca.persistence;

import com.biblioteca.model.Saga;
import java.nio.file.Path;

/**
 * Implementación de persistencia para Sagas con almacenamiento serializado.
 */
public class SagaRepositorioImpl extends ArchivoRepositorio<Saga, String> implements SagaRepositorio {

    public SagaRepositorioImpl(Path archivo) {
        super(archivo, Saga::id);
    }
}
