package com.biblioteca.ui;

import com.biblioteca.model.EstadoLectura;
import com.biblioteca.model.Genero;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Saga;
import com.biblioteca.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Controlador para la ventana modal de creación y edición de Libros.
 */
public class LibroDialogController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private Spinner<Integer> spnAnio;
    @FXML private Spinner<Integer> spnPaginas;
    @FXML private ComboBox<Genero> cmbGenero;
    @FXML private ComboBox<Saga> cmbSaga;
    @FXML private Spinner<Integer> spnNumeroEnSaga;
    @FXML private ComboBox<EstadoLectura> cmbEstado;
    @FXML private Spinner<Integer> spnCalificacion;
    @FXML private TextArea txtNotas;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private Stage dialogStage;
    private Libro libroOriginal;
    private Libro libroResultado;
    private boolean guardado = false;

    @FXML
    public void initialize() {
        int anioActual = Year.now().getValue();
        spnAnio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1000, anioActual + 5, anioActual));
        spnPaginas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 250));
        spnCalificacion.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
        spnNumeroEnSaga.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        cmbGenero.setItems(FXCollections.observableArrayList(Genero.values()));
        cmbGenero.getSelectionModel().select(Genero.FANTASIA);

        cmbEstado.setItems(FXCollections.observableArrayList(EstadoLectura.values()));
        cmbEstado.getSelectionModel().select(EstadoLectura.PENDIENTE);

        // Habilitar o deshabilitar spnNumeroEnSaga según haya saga seleccionada
        spnNumeroEnSaga.disableProperty().bind(cmbSaga.valueProperty().isNull());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setSagas(List<Saga> sagas) {
        cmbSaga.setItems(FXCollections.observableArrayList(sagas));
    }

    public void setLibro(Libro libro) {
        this.libroOriginal = libro;
        if (libro != null) {
            txtTitulo.setText(libro.titulo());
            txtAutor.setText(libro.autor());
            spnAnio.getValueFactory().setValue(libro.anio());
            spnPaginas.getValueFactory().setValue(libro.paginas());
            cmbGenero.setValue(libro.genero());
            cmbEstado.setValue(libro.estadoLectura());
            spnCalificacion.getValueFactory().setValue(libro.calificacion());
            txtNotas.setText(libro.notas());

            if (libro.sagaId() != null) {
                cmbSaga.getItems().stream()
                        .filter(s -> s.id().equals(libro.sagaId()))
                        .findFirst()
                        .ifPresent(s -> {
                            cmbSaga.setValue(s);
                            if (libro.numeroEnSaga() != null) {
                                spnNumeroEnSaga.getValueFactory().setValue(libro.numeroEnSaga());
                            }
                        });
            }
        }
    }

    @FXML
    private void handleGuardar() {
        String titulo = txtTitulo.getText();
        String autor = txtAutor.getText();

        if (titulo == null || titulo.trim().isEmpty()) {
            AlertUtils.mostrarAdvertencia("Campo Requerido", "Título obligatorio", "Por favor ingrese el título del libro.");
            return;
        }

        if (autor == null || autor.trim().isEmpty()) {
            AlertUtils.mostrarAdvertencia("Campo Requerido", "Autor obligatorio", "Por favor ingrese el autor del libro.");
            return;
        }

        Saga sagaSeleccionada = cmbSaga.getValue();
        String sagaId = (sagaSeleccionada != null) ? sagaSeleccionada.id() : null;
        Integer numVolumen = (sagaSeleccionada != null) ? spnNumeroEnSaga.getValue() : null;

        String id = (libroOriginal != null) ? libroOriginal.id() : UUID.randomUUID().toString();

        try {
            libroResultado = new Libro(
                    id,
                    titulo,
                    autor,
                    spnAnio.getValue(),
                    spnPaginas.getValue(),
                    cmbGenero.getValue(),
                    sagaId,
                    numVolumen,
                    cmbEstado.getValue(),
                    spnCalificacion.getValue(),
                    txtNotas.getText()
            );
            guardado = true;
            dialogStage.close();
        } catch (IllegalArgumentException e) {
            AlertUtils.mostrarError("Error de Validación", "Datos no válidos", e.getMessage());
        }
    }

    @FXML
    private void handleLimpiarSaga() {
        cmbSaga.setValue(null);
    }

    @FXML
    private void handleCancelar() {
        guardado = false;
        dialogStage.close();
    }

    public boolean isGuardado() {
        return guardado;
    }

    public Libro getLibroResultado() {
        return libroResultado;
    }
}
