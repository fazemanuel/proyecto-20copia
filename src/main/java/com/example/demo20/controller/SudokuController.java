package com.example.demo20.controller;

import com.example.demo20.model.SudokuModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class SudokuController implements Initializable {

    @FXML private GridPane sudokuGrid;

    @FXML private TextField cell00, cell01, cell02, cell03, cell04, cell05;
    @FXML private TextField cell10, cell11, cell12, cell13, cell14, cell15;
    @FXML private TextField cell20, cell21, cell22, cell23, cell24, cell25;
    @FXML private TextField cell30, cell31, cell32, cell33, cell34, cell35;
    @FXML private TextField cell40, cell41, cell42, cell43, cell44, cell45;
    @FXML private TextField cell50, cell51, cell52, cell53, cell54, cell55;

    @FXML private Button newGameButton;
    @FXML private Button helpButton;
    @FXML private Button validateButton;
    @FXML private Button clearButton;

    @FXML private Label statusLabel;
    @FXML private Label hintsLabel;
    @FXML private Label timeLabel;

    private SudokuModel model;
    private TextField[][] cellMatrix;
    private Timeline timer;
    private TextField selectedCell; //celda seleccionada

    private static final String NORMAL_STYLE = "-fx-border-color: #2c3e50; -fx-border-width: 2; -fx-background-color: white; -fx-font-size: 16; -fx-font-weight: bold;";
    private static final String ERROR_STYLE = "-fx-border-color: #e74c3c; -fx-border-width: 3; -fx-background-color: #ffebee; -fx-font-size: 16; -fx-font-weight: bold;";
    private static final String INITIAL_STYLE = "-fx-border-color: #2c3e50; -fx-border-width: 2; -fx-background-color: #e0e0e0; -fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #34495e;";
    private static final String SELECTED_STYLE = "-fx-border-color: #27ae60; -fx-border-width: 3; -fx-background-color: #e8f5e8; -fx-font-size: 16; -fx-font-weight: bold;";
    private static final String HINT_STYLE = "-fx-border-color: #f39c12; -fx-border-width: 3; -fx-background-color: #fef9e7; -fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #f39c12;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = new SudokuModel();
        initializeCellMatrix();
        setupCellEvents();
        setupTimer();
        updateUI();
        statusLabel.setText("¡Bienvenido al Sudoku! Haz clic en 'Nuevo Juego' para comenzar.");
    }

    private void initializeCellMatrix() {
        cellMatrix = new TextField[6][6];
        cellMatrix[0] = new TextField[]{cell00, cell01, cell02, cell03, cell04, cell05};
        cellMatrix[1] = new TextField[]{cell10, cell11, cell12, cell13, cell14, cell15};
        cellMatrix[2] = new TextField[]{cell20, cell21, cell22, cell23, cell24, cell25};
        cellMatrix[3] = new TextField[]{cell30, cell31, cell32, cell33, cell34, cell35};
        cellMatrix[4] = new TextField[]{cell40, cell41, cell42, cell43, cell44, cell45};
        cellMatrix[5] = new TextField[]{cell50, cell51, cell52, cell53, cell54, cell55};
    }

    private void setupCellEvents() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                final int r = row;
                final int c = col;
                TextField cell = cellMatrix[row][col];

                cell.setOnMouseClicked(event -> {
                    if (!model.isInitialCell(r, c)) {
                        selectCell(cell);
                    }
                });

                //cell.setOnKeyPressed(this::handleKeyPress);

                cell.textProperty().addListener((observable, oldValue, newValue) -> {
                    handleTextChange(r, c, newValue);
                });

                cell.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("[1-6]?")) {
                        cell.setText(oldValue);
                    }
                });
            }
        }
    }

    private void setupTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timer.setCycleCount(Timeline.INDEFINITE);
    }

    private void selectCell(TextField cell) {
        selectedCell = cell;
        updateCellStyles();
        cell.requestFocus();
    }


    private void handleKeyPress(KeyEvent event) {
        if (selectedCell == null) return;

        switch (event.getCode()) {
            case DELETE:
            case BACK_SPACE:
                selectedCell.setText("");
                break;
            case DIGIT1: case NUMPAD1:
                selectedCell.setText("1");
                break;
            case DIGIT2: case NUMPAD2:
                selectedCell.setText("2");
                break;
            case DIGIT3: case NUMPAD3:
                selectedCell.setText("3");
                break;
            case DIGIT4: case NUMPAD4:
                selectedCell.setText("4");
                break;
            case DIGIT5: case NUMPAD5:
                selectedCell.setText("5");
                break;
            case DIGIT6: case NUMPAD6:
                selectedCell.setText("6");
                break;
            default:
                break;
        }
    }

    private void handleTextChange(int row, int col, String newValue) {
        try {
            int value = newValue.isEmpty() ? 0 : Integer.parseInt(newValue);

            if (model.setValue(row, col, value)) {
                updateCellStyles();
                updateUI();

                if (model.isGameCompleted()) {
                    timer.stop();
                    showGameCompletedDialog();
                }
            }
        } catch (NumberFormatException e) {
        }
    }

    @FXML
    private void handleNewGame(ActionEvent event) {
        if (hasGameInProgress()) {
            Optional<ButtonType> result = showConfirmationDialog(
                    "Nuevo Juego",
                    "¿Estás seguro de que quieres iniciar un nuevo juego? Se perderá el progreso actual."
            );

            if (result.get() != ButtonType.OK) {
                return;
            }
        }

        model.startNewGame();
        updateUI();
        updateCellStyles();

        timer.stop();
        timer.play();

        statusLabel.setText("¡Nuevo juego iniciado! Completa la cuadrícula siguiendo las reglas del Sudoku.");
    }

    @FXML
    private void handleHelp(ActionEvent event) {
        if (model.getHintsRemaining() <= 0) {
            showAlert("Sin ayudas", "Ya no tienes ayudas disponibles para este juego.", Alert.AlertType.INFORMATION);
            return;
        }

        int[] hint = model.getHint();
        if (hint == null) {
            showAlert("Sin sugerencias", "No se pudo encontrar una sugerencia válida en este momento.", Alert.AlertType.INFORMATION);
            return;
        }

        int row = hint[0];
        int col = hint[1];
        int number = hint[2];

        TextField cell = cellMatrix[row][col];
        cell.setText(String.valueOf(number));

        Platform.runLater(() -> {
            cell.setStyle(HINT_STYLE);
            Timeline highlightTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> updateCellStyles()));
            highlightTimer.play();
        });

        updateUI();
        statusLabel.setText("¡Ayuda aplicada! El número " + number + " se colocó en la fila " + (row + 1) + ", columna " + (col + 1) + ".");
    }

    @FXML
    private void handleValidate(ActionEvent event) {
        model.validateAndMarkErrors();
        updateCellStyles();

        boolean hasErrors = false;
        int filledCells = 0;

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (model.getValue(row, col) != 0) {
                    filledCells++;
                }
                if (model.hasError(row, col)) {
                    hasErrors = true;
                }
            }
        }

        if (hasErrors) {
            statusLabel.setText("Se encontraron errores en el tablero. Las celdas incorrectas están resaltadas en rojo.");
            showAlert("Errores encontrados", "Hay números duplicados en filas, columnas o bloques. Revisa las celdas resaltadas.", Alert.AlertType.WARNING);
        } else if (filledCells == 36) {
            statusLabel.setText("¡Felicitaciones! Has completado el Sudoku correctamente.");
            timer.stop();
            showGameCompletedDialog();
        } else {
            statusLabel.setText("¡Muy bien! No hay errores hasta ahora. Continúa completando el tablero.");
            showAlert("Validación correcta", "No se encontraron errores en los números ingresados.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        Optional<ButtonType> result = showConfirmationDialog(
                "Limpiar Tablero",
                "¿Estás seguro de que quieres limpiar todos los números ingresados?"
        );

        if (result.get() == ButtonType.OK) {
            model.clearUserEntries();
            updateUI();
            updateCellStyles();
            statusLabel.setText("Tablero limpiado. Puedes continuar desde los números iniciales.");
        }
    }

    private void updateUI() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                int value = model.getValue(row, col);
                String text = value == 0 ? "" : String.valueOf(value);
                cellMatrix[row][col].setText(text);
                cellMatrix[row][col].setEditable(!model.isInitialCell(row, col));
            }
        }

        hintsLabel.setText(String.valueOf(model.getHintsRemaining()));
    }

    private void updateCellStyles() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                TextField cell = cellMatrix[row][col];
                String style = getCellStyle(row, col);
                cell.setStyle(style);
            }
        }
    }

    private String getCellStyle(int row, int col) {
        TextField cell = cellMatrix[row][col];

        if (model.hasError(row, col)) {
            return ERROR_STYLE;
        }

        if (cell == selectedCell) {
            return SELECTED_STYLE;
        }

        if (model.isInitialCell(row, col)) {
            return INITIAL_STYLE;
        }

        return NORMAL_STYLE;
    }

    private void updateTimer() {
        java.time.Duration elapsed = model.getElapsedTime();
        long minutes = elapsed.toMinutes();
        long seconds = elapsed.minusMinutes(minutes).getSeconds();
        timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private boolean hasGameInProgress() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (model.getValue(row, col) != 0 && !model.isInitialCell(row, col)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<ButtonType> showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Aceptar");
        ((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        return alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Aceptar");

        alert.showAndWait();
    }

    private void showGameCompletedDialog() {
        java.time.Duration elapsed = model.getElapsedTime();
        long minutes = elapsed.toMinutes();
        long seconds = elapsed.minusMinutes(minutes).getSeconds();
        String timeText = String.format("%02d:%02d", minutes, seconds);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("¡Felicitaciones!");
        alert.setHeaderText("¡Has completado el Sudoku!");
        alert.setContentText(
                "Tiempo transcurrido: " + timeText + "\n" +
                        "Ayudas utilizadas: " + (3 - model.getHintsRemaining()) + " de 3\n\n" +
                        "¡Excelente trabajo! ¿Te gustaría jugar otra partida?"
        );

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ((Button) alert.getDialogPane().lookupButton(ButtonType.YES)).setText("Nuevo Juego");
        ((Button) alert.getDialogPane().lookupButton(ButtonType.NO)).setText("Cerrar");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            handleNewGame(null);
        }

        statusLabel.setText("¡Juego completado en " + timeText + "! ¡Bien hecho!");
    }

    public void cleanup() {
        if (timer != null) {
            timer.stop();
        }
    }

    public GameStats getCurrentGameStats() {
        return new GameStats(
                model.getElapsedTime(),
                3 - model.getHintsRemaining(),
                model.isGameCompleted(),
                getCompletionPercentage()
        );
    }

    private double getCompletionPercentage() {
        int filledCells = 0;
        int totalCells = 36;

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                if (model.getValue(row, col) != 0) {
                    filledCells++;
                }
            }
        }

        return (double) filledCells / totalCells * 100;
    }

    public static class GameStats {
        private final java.time.Duration elapsedTime;
        private final int hintsUsed;
        private final boolean completed;
        private final double completionPercentage;

        public GameStats(java.time.Duration elapsedTime, int hintsUsed, boolean completed, double completionPercentage) {
            this.elapsedTime = elapsedTime;
            this.hintsUsed = hintsUsed;
            this.completed = completed;
            this.completionPercentage = completionPercentage;
        }

        public java.time.Duration getElapsedTime() { return elapsedTime; }
        public int getHintsUsed() { return hintsUsed; }
        public boolean isCompleted() { return completed; }
        public double getCompletionPercentage() { return completionPercentage; }

        @Override
        public String toString() {
            return String.format(
                    "GameStats{time=%s, hints=%d, completed=%b, progress=%.1f%%}",
                    elapsedTime, hintsUsed, completed, completionPercentage
            );
        }
    }
}