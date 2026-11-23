# Gestor de Biblioteca Personal y Sagas

Documento de especificación, arquitectura y plan de trabajo para el desarrollo del **Gestor de Biblioteca Personal y Sagas**.

---

## 1. Generalidades del Proyecto

* **Nombre del Proyecto:** Gestor de Biblioteca Personal y Sagas.
* **Propósito:** Proporcionar una herramienta eficiente, ligera y autónoma para la catalogación, seguimiento y gestión de libros físicos/digitales y sagas o series literarias personales.
* **Tipo de Aplicación:** Aplicación de escritorio empaquetada en formato **AppImage**, garantizando portabilidad, independencia de dependencias del sistema y compatibilidad directa en distribuciones **Linux**.
* **Arquitectura de Software:** Arquitectura en capas (*Layered Architecture*), promoviendo una estricta separación de responsabilidades, bajo acoplamiento y alta mantenibilidad:
  * **Capa de Presentación (UI):** Vistas y controladores para la interacción con el usuario.
  * **Capa de Lógica de Negocio (Service):** Reglas del negocio, validaciones y procesamiento de datos.
  * **Capa de Persistencia (Data Access):** Gestión de operaciones de entrada/salida sobre archivos locales.
  * **Capa de Dominio/Modelo:** Estructuras de datos inmutables y representaciones del negocio.
* **Estrategia de Persistencia:** Almacenamiento basado exclusivamente en **archivos locales**. No utiliza motores de bases de datos relacionales ni NoSQL (sin SQL, sin SQLite, sin servidores externos).

---

## 2. Especificaciones Técnicas

* **Lenguaje:** **Java** (versión moderna LTS, Java 17 o superior, requerido para el soporte completo y robusto de `record`).
* **Interfaz Gráfica de Usuario (GUI):** **JavaFX**, utilizando archivos FXML para la definición declarativa de interfaces y hojas de estilo CSS para el diseño visual.
* **Estructura del Modelo:** Entidades modeladas mediante **`record` de Java**, aprovechando su inmutabilidad intrínseca, generación automática de constructores canónicos, métodos de acceso, `equals()`, `hashCode()` y `toString()`.
* **Operaciones CRUD:** Implementación integral de las operaciones Crear (*Create*), Leer (*Read*), Actualizar (*Update*) y Eliminar (*Delete*) para libros y sagas.
* **Mecanismo de Persistencia:** **CRUD Serializado**, mediante flujos de serialización de objetos en Java (`ObjectOutputStream` y `ObjectInputStream`) aplicados a los records del dominio sobre ficheros de almacenamiento local.
* **Procesamiento de Colecciones:** Utilización de la **API de Streams** y expresiones Lambda de Java para operaciones de búsqueda, ordenamiento, filtrado dinámico y generación de estadísticas o métricas de lectura.
* **Distribución:** Empaquetado autónomo en **AppImage** para Linux mediante herramientas de empaquetado como `jpackage` y `appimagetool`.

---

## 3. Plan de Trabajo para el Proyecto

El desarrollo se estructurará en cinco fases secuenciales e incrementales:

| Fase | Denominación | Descripción |
| :--- | :--- | :--- |
| **Fase 1** | **Análisis y diseño de la arquitectura** | Definición detallada de requerimientos, diseño de la arquitectura en capas, definición de diagramas de clases, especificación del protocolo de serialización en archivos y estructura de directorios. |
| **Fase 2** | **Implementación del modelo y capa de persistencia** | Definición de los `record` del dominio (libros, sagas, categorías) y construcción del motor de almacenamiento y serialización sobre archivos locales. |
| **Fase 3** | **Desarrollo de la lógica de negocio (CRUD y Streams)** | Creación de los servicios que coordinan las operaciones CRUD, validaciones de integridad, consultas y filtros avanzados implementados con Java Streams. |
| **Fase 4** | **Construcción de la interfaz de usuario (UI)** | Maquetación con JavaFX (FXML + CSS), desarrollo de controladores, binding reactivo a datos y conexión con la capa de servicios. |
| **Fase 5** | **Pruebas integrales y documentación** | Verificación unitaria e integrativa de flujos de serialización y CRUD, empaquetado en formato AppImage para Linux y redacción de documentación técnica y de usuario. |

---

## 4. Tareas del Proyecto en Orden

A continuación se desglosan las tareas organizadas por módulos de ejecución ordenada:

### Módulo 1: Configuración inicial y diseño
* **1.1.** Configuración del entorno de desarrollo y herramienta de construcción (Maven / Gradle) con soporte para JavaFX.
* **1.2.** Definición de la estructura de paquetes según la arquitectura en capas:
  * `com.biblioteca.model`
  * `com.biblioteca.persistence`
  * `com.biblioteca.service`
  * `com.biblioteca.ui`
  * `com.biblioteca.util`
* **1.3.** Diseño formal de contratos (interfaces) entre capas para desacoplar dependencias.
* **1.4.** Planificación del formato y ruta de almacenamiento de archivos en el sistema Linux (ej. directorio de datos de usuario).

### Módulo 2: Capa de modelo
* **2.1.** Creación de records principales:
  * `Libro`: título, autor, año, sagaId, númeroEnSaga, estadoLectura, calificación, notas.
  * `Saga`: identificador, nombre, descripción, númeroTotalVolúmenes.
* **2.2.** Implementación de validaciones e invariantes en constructores compactos de los records.
* **2.3.** Configuración de compatibilidad con serialización (`java.io.Serializable`) para los records y sus tipos asociados.

### Módulo 3: Capa de persistencia
* **3.1.** Definición de interfaces de repositorio/DAO (`Repositorio<T>`, `LibroRepositorio`, `SagaRepositorio`).
* **3.2.** Implementación de la persistencia basada en serialización con `ObjectOutputStream` y `ObjectInputStream`.
* **3.3.** Manejo seguro de archivos: creación automática de directorios, copias de seguridad de respaldo antes de sobreescritura y control de bloqueos de archivo.
* **3.4.** Gestión de excepciones de Entrada/Salida (`IOException`, `ClassNotFoundException`) con mecanismos de recuperación o notificación.

### Módulo 4: Capa de lógica de negocio
* **4.1.** Creación de servicios de negocio (`LibroService`, `SagaService`).
* **4.2.** Implementación de métodos CRUD con reglas de validación (evitar duplicados, validación de pertenencia a saga, control de rangos).
* **4.3.** Implementación de consultas complejas mediante **Java Streams**:
  * Filtrado por autor, estado de lectura (pendiente, leyendo, completado) y género.
  * Búsqueda por texto con coincidencias parciales.
  * Agrupamiento y ordenamiento de libros por saga y orden de volumen.
  * Cálculo de estadísticas (total de libros leídos, promedio de calificaciones, progreso de sagas).

### Módulo 5: Capa de interfaz de usuario
* **5.1.** Diseño visual de pantallas mediante FXML:
  * Ventana principal con panel de navegación lateral.
  * Tabla/catálogo interactivo de libros y vistas tipo tarjeta para sagas.
  * Formularios modales de alta y edición con validación visual.
  * Panel de métricas y filtros dinámicos.
* **5.2.** Implementación de controladores JavaFX y vinculación de eventos con la capa de servicios.
* **5.3.** Aplicación de estilos visuales modernos mediante hojas de estilo CSS.
* **5.4.** Gestión de notificaciones, alertas y diálogos de confirmación para acciones destructivas (ej. eliminación).

### Módulo 6: Pruebas y documentación
* **6.1.** Pruebas unitarias para validación de records y lógica de Streams (JUnit 5).
* **6.2.** Pruebas de integración sobre la serialización y persistencia en disco (ciclos de escritura, lectura y actualización).
* **6.3.** Pruebas de usabilidad e interfaz gráfica de usuario en entorno Linux.
* **6.4.** Configuración de script/flujo de empaquetado para generar el ejecutable **AppImage**.
* **6.5.** Redacción de documentación técnica de arquitectura, guía de compilación y manual de usuario final.
