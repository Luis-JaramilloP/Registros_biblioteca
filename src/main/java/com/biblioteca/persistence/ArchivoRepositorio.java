package com.biblioteca.persistence;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Implementación base de repositorio genérico persistido en archivos mediante
 * serialización nativa de objetos Java.
 *
 * Ofrece:
 * - Escrituras atómicas (.tmp -> archivo final)
 * - Copias de seguridad automáticas (.bak)
 * - Sincronización concurrente con ReadWriteLock
 * - Manejo tolerante de archivos vacíos o inexistentes
 */
public abstract class ArchivoRepositorio<T extends Serializable, ID> implements Repositorio<T, ID> {

    protected final Path archivoPrincipal;
    protected final Path archivoTemporal;
    protected final Path archivoBackup;
    protected final Function<T, ID> extractorId;

    protected final Map<ID, T> cacheEnMemoria = new LinkedHashMap<>();
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ArchivoRepositorio(Path archivo, Function<T, ID> extractorId) {
        this.archivoPrincipal = Objects.requireNonNull(archivo, "La ruta del archivo no puede ser nula");
        this.archivoTemporal = archivo.resolveSibling(archivo.getFileName().toString() + ".tmp");
        this.archivoBackup = archivo.resolveSibling(archivo.getFileName().toString() + ".bak");
        this.extractorId = Objects.requireNonNull(extractorId, "El extractor de ID no puede ser nulo");
        inicializar();
    }

    private void inicializar() {
        lock.writeLock().lock();
        try {
            if (archivoPrincipal.getParent() != null && !Files.exists(archivoPrincipal.getParent())) {
                Files.createDirectories(archivoPrincipal.getParent());
            }
            if (!Files.exists(archivoPrincipal)) {
                guardarEnDisco(Collections.emptyList());
            } else {
                cargarDesdeDisco();
            }
        } catch (IOException e) {
            throw new SerializacionException("Error al inicializar archivo de datos: " + archivoPrincipal, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void guardar(T entidad) {
        Objects.requireNonNull(entidad, "La entidad no puede ser nula");
        lock.writeLock().lock();
        try {
            ID id = extractorId.apply(entidad);
            cacheEnMemoria.put(id, entidad);
            guardarEnDisco(new ArrayList<>(cacheEnMemoria.values()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void guardarTodos(List<T> entidades) {
        Objects.requireNonNull(entidades, "La lista de entidades no puede ser nula");
        lock.writeLock().lock();
        try {
            cacheEnMemoria.clear();
            for (T entidad : entidades) {
                cacheEnMemoria.put(extractorId.apply(entidad), entidad);
            }
            guardarEnDisco(new ArrayList<>(cacheEnMemoria.values()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<T> buscarPorId(ID id) {
        if (id == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(cacheEnMemoria.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<T> listarTodos() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(cacheEnMemoria.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean eliminarPorId(ID id) {
        if (id == null) return false;
        lock.writeLock().lock();
        try {
            if (cacheEnMemoria.containsKey(id)) {
                cacheEnMemoria.remove(id);
                guardarEnDisco(new ArrayList<>(cacheEnMemoria.values()));
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean existePorId(ID id) {
        if (id == null) return false;
        lock.readLock().lock();
        try {
            return cacheEnMemoria.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long contar() {
        lock.readLock().lock();
        try {
            return cacheEnMemoria.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void recargar() {
        lock.writeLock().lock();
        try {
            cargarDesdeDisco();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void cargarDesdeDisco() {
        if (!Files.exists(archivoPrincipal)) {
            cacheEnMemoria.clear();
            return;
        }

        try {
            if (Files.size(archivoPrincipal) == 0) {
                cacheEnMemoria.clear();
                return;
            }
        } catch (IOException ignored) {}

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(archivoPrincipal)))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?> lista) {
                cacheEnMemoria.clear();
                for (Object item : lista) {
                    T entidad = (T) item;
                    cacheEnMemoria.put(extractorId.apply(entidad), entidad);
                }
            }
        } catch (EOFException e) {
            cacheEnMemoria.clear();
        } catch (IOException | ClassNotFoundException e) {
            // Intentar recuperar desde el archivo backup si existe
            if (Files.exists(archivoBackup)) {
                try (ObjectInputStream backupOis = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(archivoBackup)))) {
                    Object obj = backupOis.readObject();
                    if (obj instanceof List<?> lista) {
                        cacheEnMemoria.clear();
                        for (Object item : lista) {
                            T entidad = (T) item;
                            cacheEnMemoria.put(extractorId.apply(entidad), entidad);
                        }
                        return;
                    }
                } catch (Exception exBackup) {
                    // Fallback falló también
                }
            }
            throw new SerializacionException("Error al deserializar registros desde " + archivoPrincipal, e);
        }
    }

    private void guardarEnDisco(List<T> lista) {
        try {
            // 1. Escribir primero al archivo temporal
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(archivoTemporal)))) {
                oos.writeObject(lista);
                oos.flush();
            }

            // 2. Si el archivo principal ya existe, crear copia de seguridad
            if (Files.exists(archivoPrincipal)) {
                Files.copy(archivoPrincipal, archivoBackup, StandardCopyOption.REPLACE_EXISTING);
            }

            // 3. Mover el archivo temporal al destino final (intentando movimiento atómico primero)
            try {
                Files.move(archivoTemporal, archivoPrincipal, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception exMove) {
                Files.move(archivoTemporal, archivoPrincipal, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new SerializacionException("Error durante la serialización al archivo: " + archivoPrincipal, e);
        }
    }
}
