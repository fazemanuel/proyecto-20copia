package com.example.demo20;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de la aplicación Sudoku 6x6.
 * Extiende Application de JavaFX para crear la interfaz gráfica.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class SudokuApplication extends Application {

    /**
     * Punto de entrada de la aplicación JavaFX.
     *
     * @param stage escenario principal de la aplicación
     * @throws IOException si hay problemas cargando el archivo FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        try {
            // Cargar el archivo FXML
            FXMLLoader fxmlLoader = new FXMLLoader(
                    SudokuApplication.class.getResource("sudoku-view.fxml")
            );

            // Crear la escena
            Scene scene = new Scene(fxmlLoader.load());

            // Configurar el stage
            stage.setTitle("Sudoku 6x6 - Fundamentos de Programación Orientada a Eventos");
            stage.setScene(scene);
            stage.setResizable(false); // Tamaño fijo para mejor experiencia
            stage.centerOnScreen();

            // Configurar icono si existe
            try {
                stage.getIcons().add(new Image("/images/sudoku-icon.png"));
            } catch (Exception e) {
                // Si no existe el icono, continuar sin él
                System.out.println("Icono no encontrado, continuando sin icono.");
            }

            // Configurar el comportamiento al cerrar
            stage.setOnCloseRequest(event -> {
                System.out.println("Cerrando aplicación Sudoku...");
            });

            // Mostrar la ventana
            stage.show();

            System.out.println("Aplicación Sudoku 6x6 iniciada correctamente.");

        } catch (IOException e) {
            System.err.println("Error al cargar la interfaz: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Método principal que lanza la aplicación JavaFX.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        System.out.println("Iniciando aplicación Sudoku 6x6...");
        launch(args);
    }
}