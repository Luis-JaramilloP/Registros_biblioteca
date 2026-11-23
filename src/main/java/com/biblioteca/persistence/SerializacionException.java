package com.biblioteca.persistence;

/**
 * Excepción lanzada cuando ocurre una falla durante la serialización
 * o deserialización de datos en disco.
 */
public class SerializacionException extends RuntimeException {
    public SerializacionException(String message) {
        super(message);
    }

    public SerializacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
