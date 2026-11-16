package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;

public class Controller {

    // Componentes de FXML
    @FXML private ChoiceBox<String> cbFiltroEstado;
    @FXML private ChoiceBox<String> cbEstadoTarea;
    @FXML private ChoiceBox<String> cbCambiarEstado;
    @FXML private ProgressBar pbProgresoGlobal;
    @FXML private Label lblPorcentaje;
    @FXML private Label lblPlaceholderWeb;
    @FXML private TableView<Tarea> tablaTareas;
    @FXML private TableColumn<Tarea, String> colTitulo;
    @FXML private TableColumn<Tarea, String> colEstado;
    @FXML private TableColumn<Tarea, String> colColor;
    @FXML private TextField txtNuevaTarea;
    @FXML private ColorPicker cpColorTarea;
    @FXML private Button btnAgregar;
    @FXML private TextArea txtNotas;
    @FXML private WebView webVistaPrevia;

    private final ObservableList<Tarea> listaTareas = FXCollections.observableArrayList();
    private FilteredList<Tarea> tareasFiltradas;

    @FXML
    public void initialize() {
        // 1. ChoiceBox de estados y filtro
        cbEstadoTarea.getItems().addAll("Pendiente", "En progreso", "Completada");
        cbEstadoTarea.setValue("Pendiente");

        cbFiltroEstado.getItems().addAll("Todas", "Pendiente", "En progreso", "Completada");
        cbFiltroEstado.setValue("Todas");

        // ChoiceBox para cambiar el estado: desactivado hasta que se seleccione una tarea
        cbCambiarEstado.getItems().addAll("Pendiente", "En progreso", "Completada");
        cbCambiarEstado.setDisable(true);

        // 2. Tooltips
        cpColorTarea.setTooltip(new Tooltip("Selecciona un color identificativo para la tarea"));
        btnAgregar.setTooltip(new Tooltip("Añade la tarea a la tabla superior"));
        pbProgresoGlobal.setTooltip(new Tooltip("Porcentaje de tareas completadas"));
        cbCambiarEstado.setTooltip(new Tooltip("Cambia el estado de la tarea seleccionada"));

        // 3. Columnas de la tabla
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("colorHex"));

        // La columna Color pinta la celda con el color de la tarea
        colColor.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String hex, boolean empty) {
                super.updateItem(hex, empty);
                if (empty || hex == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(hex);
                    setStyle("-fx-background-color: " + hex + "; -fx-text-fill: white; -fx-font-weight: bold;");
                }
            }
        });

        // Datos de ejemplo
        listaTareas.addAll(
                new Tarea("Preparar diapositivas", "Completada", "#2ECC71",
                        "<h3>Diapositivas</h3><ul><li>Portada</li><li>Demo</li></ul>"),
                new Tarea("Revisar FXML", "En progreso", "#F1C40F",
                        "<b>Importante:</b> Comprobar fx:id en todos los nodos.")
        );

        tareasFiltradas = new FilteredList<>(listaTareas, t -> true);
        tablaTareas.setItems(tareasFiltradas);

        // Filtrado con el ChoiceBox
        cbFiltroEstado.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                aplicarFiltro());

        // 4. Selección en la tabla -> actualiza TextArea, WebView, placeholder y ChoiceBox de estado
        tablaTareas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, nuevaSel) -> {
            if (nuevaSel != null) {
                lblPlaceholderWeb.setVisible(false);
                txtNotas.setText(nuevaSel.getNotas());
                cbCambiarEstado.setDisable(false);
                cbCambiarEstado.setValue(nuevaSel.getEstado()); // muestra el estado actual
                actualizarWebView(nuevaSel);
            } else {
                lblPlaceholderWeb.setVisible(true);
                txtNotas.clear();
                cbCambiarEstado.setValue(null);
                cbCambiarEstado.setDisable(true);
                webVistaPrevia.getEngine().loadContent("");
            }
        });

        // 5. Cambio de estado de la tarea seleccionada
        cbCambiarEstado.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, nuevoEstado) -> {
            Tarea seleccionada = tablaTareas.getSelectionModel().getSelectedItem();
            // Solo actúa si hay tarea y el estado es distinto (evita reaccionar al cargar la selección)
            if (seleccionada != null && nuevoEstado != null && !nuevoEstado.equals(seleccionada.getEstado())) {
                seleccionada.setEstado(nuevoEstado);
                tablaTareas.refresh();          // repinta la columna Estado
                actualizarProgreso();           // recalcula la ProgressBar
                actualizarWebView(seleccionada); // actualiza la ficha
                aplicarFiltro();                // si ya no cumple el filtro, desaparece de la tabla
            }
        });

        // 6. Al escribir en el TextArea se actualiza la vista previa en tiempo real
        txtNotas.textProperty().addListener((obs, oldText, newText) -> {
            Tarea seleccionada = tablaTareas.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                seleccionada.setNotas(newText);
                actualizarWebView(seleccionada);
            }
        });

        // 7. Color por defecto y progreso inicial
        cpColorTarea.setValue(Color.CORNFLOWERBLUE);
        actualizarProgreso();
    }

    @FXML
    private void agregarTarea() {
        String titulo = txtNuevaTarea.getText().trim();
        if (titulo.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "Escribe un nombre para la tarea.");
            alerta.setHeaderText(null);
            alerta.showAndWait();
            return;
        }

        // Color de JavaFX -> hexadecimal para CSS/HTML
        Color c = cpColorTarea.getValue();
        String hex = String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));

        listaTareas.add(new Tarea(titulo, cbEstadoTarea.getValue(), hex, "<p>Sin notas adicionales.</p>"));
        txtNuevaTarea.clear();
        actualizarProgreso();
    }

    /**
     * Vuelve a aplicar el filtro del ChoiceBox superior.
     * Se crea un predicado nuevo para que la FilteredList reevalúe todas las tareas
     * (no se entera sola de que ha cambiado el estado de una tarea).
     */
    private void aplicarFiltro() {
        String filtro = cbFiltroEstado.getValue();
        tareasFiltradas.setPredicate(tarea ->
                filtro == null || filtro.equals("Todas") || tarea.getEstado().equals(filtro));
    }

    private void actualizarWebView(Tarea tarea) {
        String html = "<html><body style='margin:0;'>"
                + "<div style='font-family: sans-serif; border-top: 6px solid " + tarea.getColorHex() + "; padding: 10px;'>"
                + "<h2>" + tarea.getTitulo() + "</h2>"
                + "<p><b>Estado:</b> " + tarea.getEstado() + "</p><hr>"
                + (tarea.getNotas() == null ? "" : tarea.getNotas())
                + "</div></body></html>";
        webVistaPrevia.getEngine().loadContent(html);
    }

    private void actualizarProgreso() {
        if (listaTareas.isEmpty()) {
            pbProgresoGlobal.setProgress(0);
            lblPorcentaje.setText("0%");
            pbProgresoGlobal.getTooltip().setText("No hay tareas");
            return;
        }
        long completadas = listaTareas.stream()
                .filter(t -> "Completada".equals(t.getEstado()))
                .count();
        double ratio = (double) completadas / listaTareas.size();

        pbProgresoGlobal.setProgress(ratio);
        lblPorcentaje.setText(Math.round(ratio * 100) + "%");
        pbProgresoGlobal.getTooltip().setText(completadas + " de " + listaTareas.size() + " tareas completadas");
    }
}
