package com.biblioteca;

import com.biblioteca.model.EstadoLectura;
import com.biblioteca.model.Genero;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Saga;
import com.biblioteca.persistence.LibroRepositorioImpl;
import com.biblioteca.persistence.SagaRepositorioImpl;
import com.biblioteca.service.EstadisticasService;
import com.biblioteca.service.LibroService;
import com.biblioteca.service.ProgresoSaga;
import com.biblioteca.service.ResumenBiblioteca;
import com.biblioteca.service.SagaService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Suite integral de pruebas unitarias y de integración para validar:
 * 1. Restricciones e invariantes de los Records del Modelo.
 * 2. Persistencia en disco mediante Serialización nativa con archivos temporales.
 * 3. Lógica de negocio (CRUD) y consultas avanzadas con Java Streams API.
 * 4. Cálculo estadístico y métricas de avance de lectura.
 */
public class AppTestSuite {

    private static int totalPruebas = 0;
    private static int pruebasExitosas = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  INICIANDO SUITE DE PRUEBAS DEL SISTEMA");
        System.out.println("=================================================\n");

        probarInvariantesRecordSaga();
        probarInvariantesRecordLibro();
        probarPersistenciaSerializada();
        probarLógicaDeNegocioYStreams();
        probarEstadisticasYMetricas();

        System.out.println("\n=================================================");
        System.out.printf("  RESUMEN: %d/%d pruebas completadas con ÉXITO%n", pruebasExitosas, totalPruebas);
        System.out.println("=================================================");

        if (pruebasExitosas < totalPruebas) {
            System.err.println("ALERTA: Se detectaron fallos en las pruebas.");
            System.exit(1);
        }
    }

    private static void afirmar(boolean condicion, String nombrePrueba) {
        totalPruebas++;
        if (condicion) {
            pruebasExitosas++;
            System.out.println(" [OK] " + nombrePrueba);
        } else {
            System.err.println(" [FALLO] " + nombrePrueba);
            throw new AssertionError("Fallo en la prueba: " + nombrePrueba);
        }
    }

    private static void probarInvariantesRecordSaga() {
        System.out.println("--- Pruebas de Dominio: Record Saga ---");
        Saga s = new Saga("s1", "El Archivo de las Tormentas", "Brandon Sanderson", "Saga épica", 10);
        afirmar(s.nombre().equals("El Archivo de las Tormentas"), "Saga: nombre correcto");
        afirmar(s.totalVolumenes() == 10, "Saga: total de volúmenes correcto");

        boolean errorLanzado = false;
        try {
            new Saga("s2", "", "Autor", "Desc", 5);
        } catch (IllegalArgumentException e) {
            errorLanzado = true;
        }
        afirmar(errorLanzado, "Saga: rechaza nombre vacío");

        errorLanzado = false;
        try {
            new Saga("s3", "Saga", "Autor", "Desc", 0);
        } catch (IllegalArgumentException e) {
            errorLanzado = true;
        }
        afirmar(errorLanzado, "Saga: rechaza totalVolúmenes <= 0");
    }

    private static void probarInvariantesRecordLibro() {
        System.out.println("\n--- Pruebas de Dominio: Record Libro ---");
        Libro l = new Libro("l1", "El Camino de los Reyes", "Brandon Sanderson", 2010, 1200,
                Genero.FANTASIA, "s1", 1, EstadoLectura.COMPLETADO, 5, "Obra maestra");

        afirmar(l.titulo().equals("El Camino de los Reyes"), "Libro: título correcto");
        afirmar(l.perteneceASaga(), "Libro: pertenece a saga correctamente detectado");
        afirmar(l.calificacion() == 5, "Libro: calificación correcta");

        boolean errorLanzado = false;
        try {
            new Libro("l2", "", "Autor", 2020, 100, Genero.OTRO, null, null, EstadoLectura.PENDIENTE, 3, "");
        } catch (IllegalArgumentException e) {
            errorLanzado = true;
        }
        afirmar(errorLanzado, "Libro: rechaza título vacío");

        errorLanzado = false;
        try {
            new Libro("l3", "Libro", "Autor", 2020, 100, Genero.OTRO, null, null, EstadoLectura.PENDIENTE, 6, "");
        } catch (IllegalArgumentException e) {
            errorLanzado = true;
        }
        afirmar(errorLanzado, "Libro: rechaza calificación fuera de rango (0-5)");
    }

    private static void probarPersistenciaSerializada() {
        System.out.println("\n--- Pruebas de Persistencia: Serialización sobre Archivos ---");
        try {
            Path tempDir = Files.createTempDirectory("test_biblioteca_persistencia");
            Path librosFile = tempDir.resolve("test_libros.dat");
            Path sagasFile = tempDir.resolve("test_sagas.dat");

            SagaRepositorioImpl sagaRepo = new SagaRepositorioImpl(sagasFile);
            LibroRepositorioImpl libroRepo = new LibroRepositorioImpl(librosFile);

            afirmar(sagaRepo.contar() == 0, "Persistencia: repositorio inicia vacío");

            Saga saga = new Saga("saga-1", "Dune", "Frank Herbert", "Ciencia ficción clásica", 6);
            sagaRepo.guardar(saga);
            afirmar(sagaRepo.contar() == 1, "Persistencia: guarda saga correctamente");
            afirmar(Files.exists(librosFile) || Files.exists(sagasFile), "Persistencia: archivo en disco generado");

            // Recargar repositorio desde archivo para verificar deserialización
            SagaRepositorioImpl nuevoSagaRepo = new SagaRepositorioImpl(sagasFile);
            Optional<Saga> recuperada = nuevoSagaRepo.buscarPorId("saga-1");
            afirmar(recuperada.isPresent() && recuperada.get().nombre().equals("Dune"),
                    "Persistencia: deserialización exitosa de saga desde disco");

            // Probar serialización de libros
            Libro libro1 = new Libro("b1", "Dune", "Frank Herbert", 1965, 600, Genero.CIENCIA_FICCION,
                    "saga-1", 1, EstadoLectura.COMPLETADO, 5, "Clásico");
            Libro libro2 = new Libro("b2", "Mesías de Dune", "Frank Herbert", 1969, 300, Genero.CIENCIA_FICCION,
                    "saga-1", 2, EstadoLectura.LEYENDO, 4, "Buena continuación");

            libroRepo.guardar(libro1);
            libroRepo.guardar(libro2);
            afirmar(libroRepo.contar() == 2, "Persistencia: guarda libros en archivo serializado");

            LibroRepositorioImpl nuevoLibroRepo = new LibroRepositorioImpl(librosFile);
            afirmar(nuevoLibroRepo.contar() == 2, "Persistencia: deserialización de múltiples records exitosa");

            // Limpieza
            Files.deleteIfExists(librosFile);
            Files.deleteIfExists(sagasFile);
            Files.deleteIfExists(librosFile.resolveSibling("test_libros.dat.bak"));
            Files.deleteIfExists(sagasFile.resolveSibling("test_sagas.dat.bak"));
            Files.deleteIfExists(tempDir);
        } catch (IOException e) {
            throw new RuntimeException("Error en prueba de persistencia", e);
        }
    }

    private static void probarLógicaDeNegocioYStreams() {
        System.out.println("\n--- Pruebas de Servicios: CRUD y Consultas con Streams ---");
        try {
            Path tempDir = Files.createTempDirectory("test_biblioteca_servicios");
            SagaRepositorioImpl sagaRepo = new SagaRepositorioImpl(tempDir.resolve("sagas.dat"));
            LibroRepositorioImpl libroRepo = new LibroRepositorioImpl(tempDir.resolve("libros.dat"));

            SagaService sagaService = new SagaService(sagaRepo, libroRepo);
            LibroService libroService = new LibroService(libroRepo, sagaRepo);

            // 1. Crear Saga
            Saga lotr = sagaService.crearSaga("El Señor de los Anillos", "J.R.R. Tolkien", "Trilogía clásica", 3);
            afirmar(lotr != null && lotr.id() != null, "Servicio Saga: creación exitosa con UUID");

            // 2. Crear Libros vinculados
            Libro l1 = libroService.crearLibro("La Comunidad del Anillo", "J.R.R. Tolkien", 1954, 423,
                    Genero.FANTASIA, lotr.id(), 1, EstadoLectura.COMPLETADO, 5, "Excelente inicio");
            Libro l2 = libroService.crearLibro("Las Dos Torres", "J.R.R. Tolkien", 1954, 352,
                    Genero.FANTASIA, lotr.id(), 2, EstadoLectura.COMPLETADO, 5, "Gran desarrollo");
            Libro l3 = libroService.crearLibro("El Retorno del Rey", "J.R.R. Tolkien", 1955, 416,
                    Genero.FANTASIA, lotr.id(), 3, EstadoLectura.PENDIENTE, 0, "Por leer");

            // Libro independiente
            Libro l4 = libroService.crearLibro("El Hobbit", "J.R.R. Tolkien", 1937, 310,
                    Genero.FANTASIA, null, null, EstadoLectura.COMPLETADO, 4, "Muy ameno");

            // Validar Streams: Obtener libros de la saga ordenados por volumen
            List<Libro> sagaBooks = libroService.obtenerLibrosDeSaga(lotr.id());
            afirmar(sagaBooks.size() == 3, "Streams: filtrado de libros por saga");
            afirmar(sagaBooks.get(0).numeroEnSaga() == 1 && sagaBooks.get(2).numeroEnSaga() == 3,
                    "Streams: libros de saga ordenados cronológicamente por volumen");

            // Validar Streams: Filtrado dinámico multicriterio
            List<Libro> completadosFantasia = libroService.filtrar(null, Genero.FANTASIA, EstadoLectura.COMPLETADO, null);
            afirmar(completadosFantasia.size() == 3, "Streams: filtrado combinado por Género y EstadoLectura");

            // Validar Progreso de Saga
            ProgresoSaga progreso = sagaService.calcularProgreso(lotr.id());
            afirmar(progreso.librosRegistrados() == 3, "Servicio Saga: conteo de libros registrados");
            afirmar(progreso.librosCompletados() == 2, "Servicio Saga: conteo de libros completados");
            afirmar(Math.abs(progreso.porcentajeCompletado() - (2.0 / 3.0 * 100)) < 0.1,
                    "Servicio Saga: porcentaje de lectura calculado exactamente");

            // Limpieza
            Files.walk(tempDir).sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignored) {}
            });
        } catch (IOException e) {
            throw new RuntimeException("Error en prueba de servicios", e);
        }
    }

    private static void probarEstadisticasYMetricas() {
        System.out.println("\n--- Pruebas de Estadísticas: Funciones de Reducción con Streams ---");
        try {
            Path tempDir = Files.createTempDirectory("test_biblioteca_stats");
            SagaRepositorioImpl sagaRepo = new SagaRepositorioImpl(tempDir.resolve("sagas.dat"));
            LibroRepositorioImpl libroRepo = new LibroRepositorioImpl(tempDir.resolve("libros.dat"));

            SagaService sagaService = new SagaService(sagaRepo, libroRepo);
            LibroService libroService = new LibroService(libroRepo, sagaRepo);
            EstadisticasService statsService = new EstadisticasService(libroRepo, sagaRepo);

            sagaService.crearSaga("Fundación", "Isaac Asimov", "Ciclo de la Fundación", 7);

            libroService.crearLibro("Fundación", "Isaac Asimov", 1951, 250,
                    Genero.CIENCIA_FICCION, null, null, EstadoLectura.COMPLETADO, 5, "");
            libroService.crearLibro("Fundación e Imperio", "Isaac Asimov", 1952, 280,
                    Genero.CIENCIA_FICCION, null, null, EstadoLectura.COMPLETADO, 4, "");
            libroService.crearLibro("Segunda Fundación", "Isaac Asimov", 1953, 300,
                    Genero.CIENCIA_FICCION, null, null, EstadoLectura.LEYENDO, 0, "");

            ResumenBiblioteca resumen = statsService.generarResumen();
            afirmar(resumen.totalLibros() == 3, "Estadísticas: total libros");
            afirmar(resumen.totalSagas() == 1, "Estadísticas: total sagas");
            afirmar(resumen.totalPaginasLeidas() == 530, "Estadísticas: suma de páginas leídas con mapToInt().sum()");
            afirmar(resumen.calificacionPromedio() == 4.5, "Estadísticas: promedio de calificación con average()");
            afirmar(resumen.librosPorEstado().get(EstadoLectura.COMPLETADO) == 2, "Estadísticas: groupingBy() estado completado");
            afirmar(resumen.librosPorEstado().get(EstadoLectura.LEYENDO) == 1, "Estadísticas: groupingBy() estado leyendo");

            List<Libro> top = statsService.obtenerTopCalificados(1);
            afirmar(top.size() == 1 && top.get(0).titulo().equals("Fundación"),
                    "Estadísticas: top calificados filtrado y ordenado con Streams");

            // Limpieza
            Files.walk(tempDir).sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignored) {}
            });
        } catch (IOException e) {
            throw new RuntimeException("Error en prueba de estadísticas", e);
        }
    }
}
