package com.biblioteca.ui;

import com.biblioteca.model.Saga;
import com.biblioteca.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.UUID;

/**
 * Controlador para la ventana modal de creación y edición de Sagas.
 */
public class SagaDialogController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtAutor;
    @FXML private Spinner<Integer> spnTotalVolumenes;
    @FXML private TextArea txtDescripcion;

    private Stage dialogStage;
    private Saga sagaOriginal;
    private Saga sagaResultado;
    private boolean guardado = false;

    @FXML
    public void initialize() {
        spnTotalVolumenes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 3));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setSaga(Saga saga) {
        this.sagaOriginal = saga;
        if (saga != null) {
            txtNombre.setText(saga.nombre());
            txtAutor.setText(saga.autor());
            spnTotalVolumenes.getValueFactory().setValue(saga.totalVolumenes());
            txtDescripcion.setText(saga.descripcion());
        }
    }

    @FXML
    private void handleGuardar() {
        String nombre = txtNombre.getText();
        String autor = txtAutor.getText();

        if (nombre == null || nombre.trim().isEmpty()) {
            AlertUtils.mostrarAdvertencia("Campo Requerido", "Nombre obligatorio", "Por favor ingrese el nombre de la saga.");
            return;
        }

        if (autor == null || autor.trim().isEmpty()) {
            AlertUtils.mostrarAdvertencia("Campo Requerido", "Autor obligatorio", "Por favor ingrese el autor de la saga.");
            return;
        }

        String id = (sagaOriginal != null) ? sagaOriginal.id() : UUID.randomUUID().toString();

        try {
            sagaResultado = new Saga(
                    id,
                    nombre,
                    autor,
                    txtDescripcion.getText(),
                    spnTotalVolumenes.getValue()
            );
            guardado = true;
            dialogStage.close();
        } catch (IllegalArgumentException e) {
            AlertUtils.mostrarError("Error de Validación", "Datos no válidos", e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        guardado = false;
        dialogStage.close();
    }

    public boolean isGuardado() {
        return guardado;
    }

    public Saga getSagaResultado() {
        return sagaResultado;
    }
}
