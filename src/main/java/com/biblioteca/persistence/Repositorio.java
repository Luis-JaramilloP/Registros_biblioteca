package com.biblioteca.persistence;

import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico para repositorios con persistencia en archivos.
 *
 * @param <T>  Tipo de entidad inmutable (ej. Record)
 * @param <ID> Tipo del identificador
 */
public interface Repositorio<T, ID> {

    /**
     * Guarda o actualiza una entidad en el repositorio.
     */
    void guardar(T entidad);

    /**
     * Guarda una lista completa de entidades reemplazando las existentes.
     */
    void guardarTodos(List<T> entidades);

    /**
     * Busca una entidad por su identificador único.
     */
    Optional<T> buscarPorId(ID id);

    /**
     * Lista todas las entidades almacenadas.
     */
    List<T> listarTodos();

    /**
     * Elimina una entidad por su ID.
     * @return true si fue eliminada, false si no se encontró.
     */
    boolean eliminarPorId(ID id);

    /**
     * Comprueba si existe una entidad con el identificador dado.
     */
    boolean existePorId(ID id);

    /**
     * Cantidad total de registros.
     */
    long contar();

    /**
     * Recarga los datos desde el archivo en disco.
     */
    void recargar();
}
