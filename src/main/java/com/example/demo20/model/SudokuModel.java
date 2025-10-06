package com.example.demo20.model;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class SudokuModel {

    private static final int GRID_SIZE = 6;
    private static final int BLOCK_WIDTH = 3;
    private static final int BLOCK_HEIGHT = 2;
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 6;
    private static final int MAX_SELECTION_ATTEMPTS = 500;

    private int[][] grid;
    private int[][] solutionGrid; // Guardar la solución completa
    private boolean[][] initialCells;
    private boolean[][] errorCells;
    private int hintsRemaining;
    private Instant startTime;
    private boolean gameCompleted;

    public SudokuModel() {
        this.grid = new int[GRID_SIZE][GRID_SIZE];
        this.solutionGrid = new int[GRID_SIZE][GRID_SIZE];
        this.initialCells = new boolean[GRID_SIZE][GRID_SIZE];
        this.errorCells = new boolean[GRID_SIZE][GRID_SIZE];
        this.hintsRemaining = 3;
        this.gameCompleted = false;
        initializeGrid();
    }

    private void initializeGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = 0;
                initialCells[i][j] = false;
                errorCells[i][j] = false;
            }
        }
    }

    public void startNewGame() {
        initializeGrid();
        generateInitialBoard();
        this.hintsRemaining = 3;
        this.startTime = Instant.now();
        this.gameCompleted = false;
    }

    private void generateInitialBoard() {
        System.out.println("Generando Sudoku con solución única...");

        // Paso 1: Generar UNA solución completa y válida
        generateCompleteSolution();

        // Paso 2: Guardar la solución completa
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(grid[i], 0, solutionGrid[i], 0, GRID_SIZE);
        }

        // Paso 3: Intentar diferentes selecciones de celdas iniciales
        int attempts = 0;
        boolean foundUnique = false;

        while (!foundUnique && attempts < MAX_SELECTION_ATTEMPTS) {
            attempts++;

            // Restaurar la solución completa al grid
            for (int i = 0; i < GRID_SIZE; i++) {
                System.arraycopy(solutionGrid[i], 0, grid[i], 0, GRID_SIZE);
            }

            // Limpiar marcas de celdas iniciales
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    initialCells[i][j] = false;
                }
            }

            // Seleccionar 2 números por cada bloque 3x2
            selectInitialCells();

            // Verificar si tiene solución única
            if (hasUniqueSolution()) {
                foundUnique = true;
                System.out.println("✓ Sudoku con solución única encontrado en intento #" + attempts);
            }
        }

        if (!foundUnique) {
            System.out.println("⚠ No se encontró solución única en " + attempts + " intentos. Usando el último generado.");
        }
    }

    private void selectInitialCells() {
        Random random = new Random();

        int[][] blockStarts = {
                {0, 0}, {0, 3},  // Bloques superiores
                {2, 0}, {2, 3},  // Bloques medios
                {4, 0}, {4, 3}   // Bloques inferiores
        };

        // Para cada bloque, seleccionar 2 posiciones aleatorias
        for (int[] blockStart : blockStarts) {
            int startRow = blockStart[0];
            int startCol = blockStart[1];

            // Crear lista de todas las posiciones en este bloque
            List<int[]> blockPositions = new ArrayList<>();
            for (int r = startRow; r < startRow + BLOCK_HEIGHT; r++) {
                for (int c = startCol; c < startCol + BLOCK_WIDTH; c++) {
                    blockPositions.add(new int[]{r, c});
                }
            }

            // Mezclar y seleccionar las primeras 2 posiciones
            Collections.shuffle(blockPositions, random);

            for (int i = 0; i < 2; i++) {
                int[] pos = blockPositions.get(i);
                initialCells[pos[0]][pos[1]] = true;
            }
        }

        // Limpiar las celdas que no son iniciales (ocultarlas)
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (!initialCells[row][col]) {
                    grid[row][col] = 0;
                }
            }
        }
    }

    /**
     * Verifica si el tablero actual tiene exactamente una solución única
     */
    private boolean hasUniqueSolution() {
        int[][] gridCopy = copyGrid();
        int solutionCount = countSolutions(gridCopy, 0, 0, 0);
        return solutionCount == 1;
    }

    /**
     * Cuenta el número de soluciones posibles del tablero usando backtracking
     */
    private int countSolutions(int[][] tempGrid, int row, int col, int count) {
        // Optimización: si ya encontramos más de 1 solución, detener
        if (count > 1) {
            return count;
        }

        // Si llegamos al final del tablero, encontramos una solución
        if (row == GRID_SIZE) {
            return count + 1;
        }

        // Calcular siguiente posición
        int nextRow = (col == GRID_SIZE - 1) ? row + 1 : row;
        int nextCol = (col == GRID_SIZE - 1) ? 0 : col + 1;

        // Si la celda ya tiene un valor, pasar a la siguiente
        if (tempGrid[row][col] != 0) {
            return countSolutions(tempGrid, nextRow, nextCol, count);
        }

        // Probar todos los números posibles
        for (int num = MIN_VALUE; num <= MAX_VALUE; num++) {
            if (isValidPlacementInGrid(tempGrid, row, col, num)) {
                tempGrid[row][col] = num;
                count = countSolutions(tempGrid, nextRow, nextCol, count);
                tempGrid[row][col] = 0; // Backtrack

                // Si ya encontramos múltiples soluciones, no seguir buscando
                if (count > 1) {
                    return count;
                }
            }
        }

        return count;
    }

    /**
     * Valida si un número puede colocarse en una posición específica de un grid temporal
     */
    private boolean isValidPlacementInGrid(int[][] tempGrid, int row, int col, int number) {
        // Verificar fila
        for (int c = 0; c < GRID_SIZE; c++) {
            if (c != col && tempGrid[row][c] == number) {
                return false;
            }
        }

        // Verificar columna
        for (int r = 0; r < GRID_SIZE; r++) {
            if (r != row && tempGrid[r][col] == number) {
                return false;
            }
        }

        // Verificar bloque 3x2
        int blockStartRow = (row / BLOCK_HEIGHT) * BLOCK_HEIGHT;
        int blockStartCol = (col / BLOCK_WIDTH) * BLOCK_WIDTH;

        for (int r = blockStartRow; r < blockStartRow + BLOCK_HEIGHT; r++) {
            for (int c = blockStartCol; c < blockStartCol + BLOCK_WIDTH; c++) {
                if ((r != row || c != col) && tempGrid[r][c] == number) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Crea una copia del grid actual
     */
    private int[][] copyGrid() {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, GRID_SIZE);
        }
        return copy;
    }

    /**
     * Genera un tablero de Sudoku 6x6 completo y válido usando backtracking
     */
    private void generateCompleteSolution() {
        // Limpiar el tablero
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = 0;
            }
        }

        // Llenar con backtracking
        fillGridRandomized(0, 0);
    }

    /**
     * Llena el tablero recursivamente con números aleatorios válidos
     */
    private boolean fillGridRandomized(int row, int col) {
        // Si llegamos al final del tablero, hemos terminado
        if (row == GRID_SIZE) {
            return true;
        }

        // Calcular la siguiente posición
        int nextRow = (col == GRID_SIZE - 1) ? row + 1 : row;
        int nextCol = (col == GRID_SIZE - 1) ? 0 : col + 1;

        // Crear lista de números del 1 al 6 en orden aleatorio
        List<Integer> numbers = new ArrayList<>();
        for (int i = MIN_VALUE; i <= MAX_VALUE; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers, new Random());

        // Probar cada número en orden aleatorio
        for (int num : numbers) {
            if (isValidPlacement(row, col, num)) {
                grid[row][col] = num;

                if (fillGridRandomized(nextRow, nextCol)) {
                    return true;
                }

                // Backtrack
                grid[row][col] = 0;
            }
        }

        return false;
    }

    public boolean isValidPlacement(int row, int col, int number) {
        if (number < MIN_VALUE || number > MAX_VALUE) {
            return false;
        }

        int originalValue = grid[row][col];
        grid[row][col] = 0;

        // Verificar fila
        for (int c = 0; c < GRID_SIZE; c++) {
            if (grid[row][c] == number) {
                grid[row][col] = originalValue;
                return false;
            }
        }

        // Verificar columna
        for (int r = 0; r < GRID_SIZE; r++) {
            if (grid[r][col] == number) {
                grid[row][col] = originalValue;
                return false;
            }
        }

        // Verificar bloque 3x2
        int blockStartRow = (row / BLOCK_HEIGHT) * BLOCK_HEIGHT;
        int blockStartCol = (col / BLOCK_WIDTH) * BLOCK_WIDTH;

        for (int r = blockStartRow; r < blockStartRow + BLOCK_HEIGHT; r++) {
            for (int c = blockStartCol; c < blockStartCol + BLOCK_WIDTH; c++) {
                if (grid[r][c] == number) {
                    grid[row][col] = originalValue;
                    return false;
                }
            }
        }

        grid[row][col] = originalValue;
        return true;
    }

    public boolean setValue(int row, int col, int value) {
        if (isInitialCell(row, col)) {
            return false;
        }

        if (value == 0) {
            grid[row][col] = 0;
            errorCells[row][col] = false;
            return true;
        }

        if (value < MIN_VALUE || value > MAX_VALUE) {
            return false;
        }

        grid[row][col] = value;
        validateAndMarkErrors();
        checkGameCompletion();

        return true;
    }

    public void validateAndMarkErrors() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                errorCells[i][j] = false;
            }
        }

        // Validar filas
        for (int row = 0; row < GRID_SIZE; row++) {
            Set<Integer> seen = new HashSet<>();
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = grid[row][col];
                if (value != 0) {
                    if (seen.contains(value)) {
                        for (int c = 0; c < GRID_SIZE; c++) {
                            if (grid[row][c] == value) {
                                errorCells[row][c] = true;
                            }
                        }
                    } else {
                        seen.add(value);
                    }
                }
            }
        }

        // Validar columnas
        for (int col = 0; col < GRID_SIZE; col++) {
            Set<Integer> seen = new HashSet<>();
            for (int row = 0; row < GRID_SIZE; row++) {
                int value = grid[row][col];
                if (value != 0) {
                    if (seen.contains(value)) {
                        for (int r = 0; r < GRID_SIZE; r++) {
                            if (grid[r][col] == value) {
                                errorCells[r][col] = true;
                            }
                        }
                    } else {
                        seen.add(value);
                    }
                }
            }
        }

        // Validar bloques 3x2
        int[][] blockStarts = {
                {0, 0}, {0, 3},
                {2, 0}, {2, 3},
                {4, 0}, {4, 3}
        };

        for (int[] blockStart : blockStarts) {
            int startRow = blockStart[0];
            int startCol = blockStart[1];

            Set<Integer> seen = new HashSet<>();
            List<int[]> positions = new ArrayList<>();

            for (int r = startRow; r < startRow + 2; r++) {
                for (int c = startCol; c < startCol + 3; c++) {
                    int value = grid[r][c];
                    if (value != 0) {
                        positions.add(new int[]{r, c, value});
                    }
                }
            }

            for (int[] pos : positions) {
                int value = pos[2];
                if (seen.contains(value)) {
                    for (int[] p : positions) {
                        if (p[2] == value) {
                            errorCells[p[0]][p[1]] = true;
                        }
                    }
                } else {
                    seen.add(value);
                }
            }
        }
    }

    public int[] getHint() {
        if (hintsRemaining <= 0) {
            return null;
        }

        List<int[]> emptyCells = new ArrayList<>();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col] == 0 && !isInitialCell(row, col)) {
                    emptyCells.add(new int[]{row, col});
                }
            }
        }

        if (emptyCells.isEmpty()) {
            return null;
        }

        Random random = new Random();
        Collections.shuffle(emptyCells, random);

        for (int[] cell : emptyCells) {
            int row = cell[0];
            int col = cell[1];

            List<Integer> validNumbers = new ArrayList<>();
            for (int num = MIN_VALUE; num <= MAX_VALUE; num++) {
                if (isValidPlacement(row, col, num)) {
                    validNumbers.add(num);
                }
            }

            if (!validNumbers.isEmpty()) {
                hintsRemaining--;
                return new int[]{row, col, validNumbers.get(random.nextInt(validNumbers.size()))};
            }
        }

        return null;
    }

    private void checkGameCompletion() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col] == 0) {
                    gameCompleted = false;
                    return;
                }
            }
        }

        validateAndMarkErrors();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (errorCells[row][col]) {
                    gameCompleted = false;
                    return;
                }
            }
        }

        gameCompleted = true;
    }

    public void clearUserEntries() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (!initialCells[row][col]) {
                    grid[row][col] = 0;
                    errorCells[row][col] = false;
                }
            }
        }
        gameCompleted = false;
    }

    public int getValue(int row, int col) {
        return grid[row][col];
    }

    public boolean isInitialCell(int row, int col) {
        return initialCells[row][col];
    }

    public boolean hasError(int row, int col) {
        return errorCells[row][col];
    }

    public int getHintsRemaining() {
        return hintsRemaining;
    }

    public boolean isGameCompleted() {
        return gameCompleted;
    }

    public Duration getElapsedTime() {
        if (startTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(startTime, Instant.now());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                sb.append(grid[row][col]).append(" ");
                if (col == 2) sb.append("| ");
            }
            sb.append("\n");
            if (row == 1 || row == 3) {
                sb.append("------+-------\n");
            }
        }
        return sb.toString();
    }
}