package com.biblioteca.ui;

import com.biblioteca.model.EstadoLectura;
import com.biblioteca.model.Genero;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Saga;
import com.biblioteca.service.EstadisticasService;
import com.biblioteca.service.LibroService;
import com.biblioteca.service.ProgresoSaga;
import com.biblioteca.service.ResumenBiblioteca;
import com.biblioteca.service.SagaService;
import com.biblioteca.util.AlertUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Controlador principal de la interfaz gráfica de usuario.
 * Coordina las vistas de Libros, Sagas y Estadísticas.
 */
public class MainController {

    // Contenedores principales de navegación
    @FXML private VBox panelLibros;
    @FXML private VBox panelSagas;
    @FXML private VBox panelEstadisticas;
    @FXML private Button btnNavLibros;
    @FXML private Button btnNavSagas;
    @FXML private Button btnNavEstadisticas;

    // Vista de Libros
    @FXML private TextField txtBuscarLibro;
    @FXML private ComboBox<Genero> cmbFiltroGenero;
    @FXML private ComboBox<EstadoLectura> cmbFiltroEstado;
    @FXML private ComboBox<FiltroSagaItem> cmbFiltroSaga;
    @FXML private TableView<Libro> tblLibros;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, Integer> colAnio;
    @FXML private TableColumn<Libro, String> colGenero;
    @FXML private TableColumn<Libro, String> colSaga;
    @FXML private TableColumn<Libro, String> colEstado;
    @FXML private TableColumn<Libro, String> colCalificacion;
    @FXML private TableColumn<Libro, Integer> colPaginas;
    @FXML private Label lblConteoLibros;

    // Vista de Sagas
    @FXML private TextField txtBuscarSaga;
    @FXML private TableView<ProgresoSaga> tblSagas;
    @FXML private TableColumn<ProgresoSaga, String> colSagaNombre;
    @FXML private TableColumn<ProgresoSaga, String> colSagaAutor;
    @FXML private TableColumn<ProgresoSaga, Integer> colSagaTotalVol;
    @FXML private TableColumn<ProgresoSaga, String> colSagaProgreso;
    @FXML private TableColumn<ProgresoSaga, String> colSagaDescripcion;
    @FXML private Label lblConteoSagas;

    // Vista de Estadísticas
    @FXML private Label lblStatTotalLibros;
    @FXML private Label lblStatTotalSagas;
    @FXML private Label lblStatPaginasLeidas;
    @FXML private Label lblStatPromedioCalificacion;
    @FXML private Label lblStatCompletados;
    @FXML private Label lblStatLeyendo;
    @FXML private Label lblStatPendientes;
    @FXML private Label lblStatAbandonados;
    @FXML private TableView<Libro> tblTopLibros;
    @FXML private TableColumn<Libro, String> colTopTitulo;
    @FXML private TableColumn<Libro, String> colTopAutor;
    @FXML private TableColumn<Libro, String> colTopCalificacion;

    // Servicios
    private LibroService libroService;
    private SagaService sagaService;
    private EstadisticasService estadisticasService;
    private Stage primaryStage;

    private final ObservableList<Libro> listaLibros = FXCollections.observableArrayList();
    private final ObservableList<ProgresoSaga> listaSagas = FXCollections.observableArrayList();

    public record FiltroSagaItem(String id, String etiqueta) {
        @Override
        public String toString() {
            return etiqueta;
        }
    }

    public void initServices(LibroService libroService, SagaService sagaService,
                             EstadisticasService estadisticasService, Stage primaryStage) {
        this.libroService = libroService;
        this.sagaService = sagaService;
        this.estadisticasService = estadisticasService;
        this.primaryStage = primaryStage;

        configurarTablas();
        configurarFiltros();
        cargarDatos();
        mostrarPanelLibros();
    }

    private void configurarTablas() {
        // Configuración de tabla de libros
        colTitulo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().titulo()));
        colAutor.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().autor()));
        colAnio.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().anio()).asObject());
        colGenero.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().genero().getDescripcion()));
        colSaga.setCellValueFactory(cell -> {
            Libro libro = cell.getValue();
            if (libro.perteneceASaga()) {
                Optional<Saga> sagaOpt = sagaService.buscarPorId(libro.sagaId());
                String nombreSaga = sagaOpt.map(Saga::nombre).orElse("Saga desconocida");
                String vol = (libro.numeroEnSaga() != null) ? " (Vol. " + libro.numeroEnSaga() + ")" : "";
                return new SimpleStringProperty(nombreSaga + vol);
            }
            return new SimpleStringProperty("— Independiente —");
        });
        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().estadoLectura().getEtiqueta()));
        colCalificacion.setCellValueFactory(cell -> {
            int cal = cell.getValue().calificacion();
            if (cal <= 0) return new SimpleStringProperty("Sin calificar");
            return new SimpleStringProperty("★".repeat(cal) + "☆".repeat(5 - cal) + " (" + cal + "/5)");
        });
        colPaginas.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().paginas()).asObject());

        tblLibros.setItems(listaLibros);

        // Configuración de tabla de sagas
        colSagaNombre.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().saga().nombre()));
        colSagaAutor.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().saga().autor()));
        colSagaTotalVol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().saga().totalVolumenes()).asObject());
        colSagaProgreso.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEtiquetaProgreso()));
        colSagaDescripcion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().saga().descripcion()));

        tblSagas.setItems(listaSagas);

        // Tabla Top Libros en Estadísticas
        colTopTitulo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().titulo()));
        colTopAutor.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().autor()));
        colTopCalificacion.setCellValueFactory(cell -> new SimpleStringProperty("★".repeat(cell.getValue().calificacion()) + " (" + cell.getValue().calificacion() + "/5)"));
    }

    private void configurarFiltros() {
        cmbFiltroGenero.setConverter(new javafx.util.StringConverter<Genero>() {
            @Override
            public String toString(Genero g) {
                return (g == null) ? "Todos los géneros" : g.getDescripcion();
            }
            @Override
            public Genero fromString(String string) {
                return null;
            }
        });

        ObservableList<Genero> generos = FXCollections.observableArrayList();
        generos.add(null);
        generos.addAll(Genero.values());
        cmbFiltroGenero.setItems(generos);
        cmbFiltroGenero.getSelectionModel().selectFirst();

        cmbFiltroEstado.setConverter(new javafx.util.StringConverter<EstadoLectura>() {
            @Override
            public String toString(EstadoLectura e) {
                return (e == null) ? "Todos los estados" : e.getEtiqueta();
            }
            @Override
            public EstadoLectura fromString(String string) {
                return null;
            }
        });

        ObservableList<EstadoLectura> estados = FXCollections.observableArrayList();
        estados.add(null);
        estados.addAll(EstadoLectura.values());
        cmbFiltroEstado.setItems(estados);
        cmbFiltroEstado.getSelectionModel().selectFirst();

        txtBuscarLibro.textProperty().addListener((obs, old, val) -> aplicarFiltroLibros());
        cmbFiltroGenero.valueProperty().addListener((obs, old, val) -> aplicarFiltroLibros());
        cmbFiltroEstado.valueProperty().addListener((obs, old, val) -> aplicarFiltroLibros());
        cmbFiltroSaga.valueProperty().addListener((obs, old, val) -> aplicarFiltroLibros());

        txtBuscarSaga.textProperty().addListener((obs, old, val) -> aplicarFiltroSagas());
    }

    private void actualizarComboSagas() {
        ObservableList<FiltroSagaItem> items = FXCollections.observableArrayList();
        items.add(new FiltroSagaItem(null, "Todas las sagas"));
        items.add(new FiltroSagaItem("__INDEPENDIENTES__", "Solo independientes"));
        for (Saga s : sagaService.listarTodas()) {
            items.add(new FiltroSagaItem(s.id(), s.nombre()));
        }
        FiltroSagaItem seleccionado = cmbFiltroSaga.getValue();
        cmbFiltroSaga.setItems(items);
        if (seleccionado != null) {
            items.stream()
                    .filter(i -> (i.id() == null && seleccionado.id() == null) || (i.id() != null && i.id().equals(seleccionado.id())))
                    .findFirst()
                    .ifPresent(cmbFiltroSaga::setValue);
        } else {
            cmbFiltroSaga.getSelectionModel().selectFirst();
        }
    }

    public void cargarDatos() {
        actualizarComboSagas();
        aplicarFiltroLibros();
        aplicarFiltroSagas();
        actualizarEstadisticas();
    }

    private void aplicarFiltroLibros() {
        String texto = txtBuscarLibro.getText();
        Genero genero = cmbFiltroGenero.getValue();
        EstadoLectura estado = cmbFiltroEstado.getValue();
        FiltroSagaItem sagaItem = cmbFiltroSaga.getValue();
        String sagaId = (sagaItem != null) ? sagaItem.id() : null;

        List<Libro> filtrados = libroService.filtrar(texto, genero, estado, sagaId);
        listaLibros.setAll(filtrados);
        lblConteoLibros.setText("Total libros mostrados: " + filtrados.size());
    }

    private void aplicarFiltroSagas() {
        String termino = txtBuscarSaga.getText();
        List<Saga> sagas = (termino == null || termino.isBlank())
                ? sagaService.listarTodas()
                : sagaService.buscar(termino);

        List<ProgresoSaga> progresos = sagas.stream()
                .map(s -> sagaService.calcularProgreso(s.id()))
                .toList();
        listaSagas.setAll(progresos);
        lblConteoSagas.setText("Total sagas mostradas: " + progresos.size());
    }

    private void actualizarEstadisticas() {
        ResumenBiblioteca resumen = estadisticasService.generarResumen();
        lblStatTotalLibros.setText(String.valueOf(resumen.totalLibros()));
        lblStatTotalSagas.setText(String.valueOf(resumen.totalSagas()));
        lblStatPaginasLeidas.setText(String.valueOf(resumen.totalPaginasLeidas()));
        lblStatPromedioCalificacion.setText(String.format("%.1f ★", resumen.calificacionPromedio()));

        Map<EstadoLectura, Long> estados = resumen.librosPorEstado();
        lblStatCompletados.setText(String.valueOf(estados.getOrDefault(EstadoLectura.COMPLETADO, 0L)));
        lblStatLeyendo.setText(String.valueOf(estados.getOrDefault(EstadoLectura.LEYENDO, 0L)));
        lblStatPendientes.setText(String.valueOf(estados.getOrDefault(EstadoLectura.PENDIENTE, 0L)));
        lblStatAbandonados.setText(String.valueOf(estados.getOrDefault(EstadoLectura.ABANDONADO, 0L)));

        tblTopLibros.setItems(FXCollections.observableArrayList(estadisticasService.obtenerTopCalificados(5)));
    }

    // Navegación
    @FXML
    public void mostrarPanelLibros() {
        panelLibros.setVisible(true);
        panelLibros.setManaged(true);
        panelSagas.setVisible(false);
        panelSagas.setManaged(false);
        panelEstadisticas.setVisible(false);
        panelEstadisticas.setManaged(false);

        btnNavLibros.getStyleClass().add("active-nav");
        btnNavSagas.getStyleClass().remove("active-nav");
        btnNavEstadisticas.getStyleClass().remove("active-nav");
        aplicarFiltroLibros();
    }

    @FXML
    public void mostrarPanelSagas() {
        panelLibros.setVisible(false);
        panelLibros.setManaged(false);
        panelSagas.setVisible(true);
        panelSagas.setManaged(true);
        panelEstadisticas.setVisible(false);
        panelEstadisticas.setManaged(false);

        btnNavLibros.getStyleClass().remove("active-nav");
        btnNavSagas.getStyleClass().add("active-nav");
        btnNavEstadisticas.getStyleClass().remove("active-nav");
        aplicarFiltroSagas();
    }

    @FXML
    public void mostrarPanelEstadisticas() {
        panelLibros.setVisible(false);
        panelLibros.setManaged(false);
        panelSagas.setVisible(false);
        panelSagas.setManaged(false);
        panelEstadisticas.setVisible(true);
        panelEstadisticas.setManaged(true);

        btnNavLibros.getStyleClass().remove("active-nav");
        btnNavSagas.getStyleClass().remove("active-nav");
        btnNavEstadisticas.getStyleClass().add("active-nav");
        actualizarEstadisticas();
    }

    // Acciones Libros
    @FXML
    private void handleNuevoLibro() {
        abrirDialogoLibro(null);
    }

    @FXML
    private void handleEditarLibro() {
        Libro seleccionado = tblLibros.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.mostrarAdvertencia("Selección requerida", "Ningún libro seleccionado", "Por favor seleccione un libro de la lista para editar.");
            return;
        }
        abrirDialogoLibro(seleccionado);
    }

    @FXML
    private void handleEliminarLibro() {
        Libro seleccionado = tblLibros.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.mostrarAdvertencia("Selección requerida", "Ningún libro seleccionado", "Por favor seleccione un libro de la lista para eliminar.");
            return;
        }

        boolean confirmado = AlertUtils.confirmar(
                "Confirmar Eliminación",
                "¿Desea eliminar este libro?",
                "Se eliminará el libro '" + seleccionado.titulo() + "'. Esta acción no se puede deshacer."
        );

        if (confirmado) {
            libroService.eliminarLibro(seleccionado.id());
            cargarDatos();
        }
    }

    @FXML
    private void handleLimpiarFiltros() {
        txtBuscarLibro.clear();
        cmbFiltroGenero.setValue(null);
        cmbFiltroEstado.setValue(null);
        cmbFiltroSaga.getSelectionModel().selectFirst();
    }

    private void abrirDialogoLibro(Libro libro) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/libro_dialog.fxml"));
            Parent root = loader.load();

            LibroDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle((libro == null) ? "Registrar Nuevo Libro" : "Editar Libro");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);
            controller.setSagas(sagaService.listarTodas());
            controller.setLibro(libro);

            dialogStage.showAndWait();

            if (controller.isGuardado()) {
                Libro resultado = controller.getLibroResultado();
                if (libro == null) {
                    libroService.crearLibro(resultado.titulo(), resultado.autor(), resultado.anio(),
                            resultado.paginas(), resultado.genero(), resultado.sagaId(),
                            resultado.numeroEnSaga(), resultado.estadoLectura(),
                            resultado.calificacion(), resultado.notas());
                } else {
                    libroService.actualizarLibro(resultado);
                }
                cargarDatos();
            }
        } catch (IOException e) {
            AlertUtils.mostrarError("Error", "Error al cargar la ventana", e.getMessage());
        } catch (Exception e) {
            AlertUtils.mostrarError("Error de Validación", "No se pudo guardar el libro", e.getMessage());
        }
    }

    // Acciones Sagas
    @FXML
    private void handleNuevaSaga() {
        abrirDialogoSaga(null);
    }

    @FXML
    private void handleEditarSaga() {
        ProgresoSaga seleccionada = tblSagas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            AlertUtils.mostrarAdvertencia("Selección requerida", "Ninguna saga seleccionada", "Por favor seleccione una saga de la lista para editar.");
            return;
        }
        abrirDialogoSaga(seleccionada.saga());
    }

    @FXML
    private void handleEliminarSaga() {
        ProgresoSaga seleccionada = tblSagas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            AlertUtils.mostrarAdvertencia("Selección requerida", "Ninguna saga seleccionada", "Por favor seleccione una saga de la lista para eliminar.");
            return;
        }

        boolean confirmado = AlertUtils.confirmar(
                "Confirmar Eliminación de Saga",
                "¿Desea eliminar la saga '" + seleccionada.saga().nombre() + "'?",
                "Los libros asociados a esta saga pasarán a ser considerados independientes."
        );

        if (confirmado) {
            sagaService.eliminarSaga(seleccionada.saga().id(), true);
            cargarDatos();
        }
    }

    @FXML
    private void handleVerLibrosDeSaga() {
        ProgresoSaga seleccionada = tblSagas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            AlertUtils.mostrarAdvertencia("Selección requerida", "Ninguna saga seleccionada", "Por favor seleccione una saga para ver sus libros.");
            return;
        }
        mostrarPanelLibros();
        // Filtrar por la saga seleccionada
        for (FiltroSagaItem item : cmbFiltroSaga.getItems()) {
            if (item.id() != null && item.id().equals(seleccionada.saga().id())) {
                cmbFiltroSaga.setValue(item);
                break;
            }
        }
    }

    private void abrirDialogoSaga(Saga saga) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/saga_dialog.fxml"));
            Parent root = loader.load();

            SagaDialogController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle((saga == null) ? "Registrar Nueva Saga" : "Editar Saga");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);
            controller.setSaga(saga);

            dialogStage.showAndWait();

            if (controller.isGuardado()) {
                Saga resultado = controller.getSagaResultado();
                if (saga == null) {
                    sagaService.crearSaga(resultado.nombre(), resultado.autor(), resultado.descripcion(), resultado.totalVolumenes());
                } else {
                    sagaService.actualizarSaga(resultado);
                }
                cargarDatos();
            }
        } catch (IOException e) {
            AlertUtils.mostrarError("Error", "Error al cargar la ventana", e.getMessage());
        } catch (Exception e) {
            AlertUtils.mostrarError("Error de Validación", "No se pudo guardar la saga", e.getMessage());
        }
    }
}
