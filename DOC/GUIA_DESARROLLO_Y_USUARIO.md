# Guía Técnica, de Desarrollo y Manual de Usuario

## Gestor de Biblioteca Personal y Sagas Literarias

Esta guía describe en detalle la implementación técnica, arquitectura en capas, persistencia serializada con `record`, instrucciones de ejecución y empaquetado como aplicación de escritorio para Linux.

---

## 1. Arquitectura del Sistema (Capas Desacopladas)

El proyecto sigue una estricta **arquitectura en capas (*Layered Architecture*)** donde cada nivel tiene una responsabilidad única:

```
┌────────────────────────────────────────────────────────┐
│                   Capa de Presentación (UI)           │
│  - MainController (JavaFX, FXML, CSS reactivo)         │
│  - LibroDialogController & SagaDialogController       │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│              Capa de Lógica de Negocio (Service)       │
│  - LibroService (Validaciones, CRUD, Streams)          │
│  - SagaService (Cálculo de progreso, filtros)          │
│  - EstadisticasService (Agregaciones con Streams API)  │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│              Capa de Persistencia (Data Access)        │
│  - Repositorio<T, ID> (Interfaz genérica)              │
│  - ArchivoRepositorio<T, ID> (Serialización atómica)   │
│  - LibroRepositorioImpl & SagaRepositorioImpl          │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                 Capa de Modelo (Dominio)               │
│  - Records inmutables: Libro, Saga                     │
│  - Enums: Genero, EstadoLectura                        │
└────────────────────────────────────────────────────────┘
```

---

## 2. Especificaciones Técnicas y Decisiones de Diseño

1. **Modelo con `record` de Java**:
   * Los registros principales `Libro` y `Saga` están diseñados como `record` de Java, aprovechando constructores compactos para validar invariantes (ej. no permitir nombres vacíos, calificaciones fuera de 0-5 o páginas negativas).
   * Al ser inmutables por diseño, eliminan efectos secundarios accidentales durante su manipulación en memoria o en la interfaz.

2. **Persistencia sobre Archivos Locales (Sin Bases de Datos)**:
   * **Serialización Segura de Objetos**: Se implementó `ArchivoRepositorio` utilizando `ObjectOutputStream` y `ObjectInputStream`.
   * **Escritura Atómica**: Los datos se escriben primero en un fichero temporal (`.tmp`), se genera una copia de seguridad (`.bak`) del archivo previo, y luego se efectúa un reemplazo atómico (`StandardCopyOption.ATOMIC_MOVE`).
   * **Sincronización Concurrente**: Se emplea `ReentrantReadWriteLock` para permitir múltiples lecturas simultáneas y garantizar exclusión mutua en escrituras.
   * **Directorio Estándar**: Sigue la especificación XDG de Linux (`~/.local/share/biblioteca_personal/`), con respaldo automático al directorio `./data/`.

3. **Consultas Avanzadas con Java Streams API**:
   * Filtrado multicriterio dinámico (combinando simultáneamente texto, género, estado de lectura y saga).
   * Agrupamiento y conteos con `Collectors.groupingBy()`.
   * Reducciones y promedios con `mapToInt().average()` y `mapToInt().sum()`.
   * Ordenación de libros dentro de sagas por volumen con comparadores encadenados.

4. **Interfaz Gráfica (JavaFX + CSS)**:
   * Vistas declarativas en FXML (`main.fxml`, `libro_dialog.fxml`, `saga_dialog.fxml`).
   * Hoja de estilos moderna (`styles.css`) con soporte para badges de estado (Completado, Leyendo, Pendiente, Abandonado), tarjetas de métricas tipo dashboard y tabla interactiva.
   * Ventanas modales para altas y ediciones con validación inmediata.

---

## 3. Estructura de Directorios del Código Fuente

```
Registros_biblioteca/
├── DOC/
│   ├── PROYECTO.md                      # Especificación inicial y plan de trabajo
│   └── GUIA_DESARROLLO_Y_USUARIO.md     # Documentación técnica y manual
├── src/
│   ├── main/
│   │   ├── java/com/biblioteca/
│   │   │   ├── Launcher.java            # Punto de entrada para empaquetado
│   │   │   ├── MainApp.java             # Aplicación principal JavaFX
│   │   │   ├── model/                   # Capa de Modelo (Records y Enums)
│   │   │   │   ├── EstadoLectura.java
│   │   │   │   ├── Genero.java
│   │   │   │   ├── Libro.java
│   │   │   │   └── Saga.java
│   │   │   ├── persistence/             # Capa de Persistencia (Serialización)
│   │   │   │   ├── ArchivoRepositorio.java
│   │   │   │   ├── LibroRepositorio.java
│   │   │   │   ├── LibroRepositorioImpl.java
│   │   │   │   ├── Repositorio.java
│   │   │   │   ├── SagaRepositorio.java
│   │   │   │   ├── SagaRepositorioImpl.java
│   │   │   │   └── SerializacionException.java
│   │   │   ├── service/                 # Capa de Negocio y Streams
│   │   │   │   ├── EstadisticasService.java
│   │   │   │   ├── LibroService.java
│   │   │   │   ├── ProgresoSaga.java
│   │   │   │   ├── ResumenBiblioteca.java
│   │   │   │   └── SagaService.java
│   │   │   ├── ui/                      # Capa de Presentación
│   │   │   │   ├── LibroDialogController.java
│   │   │   │   ├── MainController.java
│   │   │   │   └── SagaDialogController.java
│   │   │   └── util/                    # Utilidades y Rutas
│   │   │       ├── AlertUtils.java
│   │   │       └── StorageConfig.java
│   │   └── resources/
│   │       ├── css/styles.css           # Estilos de la UI
│   │       └── fxml/                    # Vistas FXML
│   │           ├── libro_dialog.fxml
│   │           ├── main.fxml
│   │           └── saga_dialog.fxml
│   └── test/java/com/biblioteca/
│       └── AppTestSuite.java            # Suite completa de pruebas
├── build.sh                             # Script de compilación y empaquetado JAR
├── test.sh                              # Script para correr pruebas unitarias
├── run.sh                               # Script de ejecución directa
├── package-appimage.sh                  # Generador de paquete AppDir/AppImage
└── pom.xml                              # Descriptor Maven para IDEs
```

---

## 4. Guía de Uso de los Scripts

### 1. Compilación
Compila todo el código fuente y genera el archivo JAR en `target/registros-biblioteca-1.0.0.jar`:
```bash
./build.sh
```

### 2. Ejecución de Pruebas Unitarias y de Integración
Ejecuta la suite completa de 29 pruebas automáticas:
```bash
./test.sh
```

### 3. Ejecutar la Aplicación en el Escritorio
Inicia la interfaz gráfica de usuario con JavaFX:
```bash
./run.sh
```

### 4. Empaquetar como AppImage / AppDir para Linux
Genera el paquete portable para Linux en el directorio `dist/`:
```bash
./package-appimage.sh
```
Una vez generado, se puede ejecutar directamente:
```bash
./dist/BibliotecaPersonal-x86_64.AppImage
```
o ejecutando directamente su `AppRun`:
```bash
./dist/BibliotecaPersonal.AppDir/AppRun
```

---

## 5. Funcionalidades del Gestor

1. **Catálogo de Libros**:
   * Registro completo de obras con título, autor, año, número de páginas, calificación (0 a 5 estrellas), estado de lectura y notas.
   * Filtros dinámicos en tiempo real combinables: texto libre, categoría literaria, estado de lectura y saga asociada.
   * Acciones CRUD (Crear, Editar, Eliminar y Detalle).

2. **Sagas y Series**:
   * Registro de sagas con nombre, autor, número previsto de tomos y descripción.
   * Indicador interactivo de progreso de lectura (volúmenes registrados y leídos frente al total planificado con porcentaje).
   * Botón directo *"Ver Libros de Saga"* que filtra automáticamente el catálogo.
   * Al eliminar una saga, el sistema ofrece desvincular los libros preservándolos como independientes o eliminarlos en cascada.

3. **Métricas y Estadísticas con Streams**:
   * Total de libros y sagas registradas.
   * Suma automática de páginas leídas de libros completados.
   * Promedio global de calificaciones.
   * Tarjetas por estado de lectura (Completados, Leyendo, Pendientes, Abandonados).
   * Tabla con el Top de libros mejor calificados calculados con Streams.
