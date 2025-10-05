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

    private int[][] grid;
    private boolean[][] initialCells;
    private boolean[][] errorCells;
    private int hintsRemaining;
    private Instant startTime;
    private boolean gameCompleted;

    public SudokuModel() {
        this.grid = new int[GRID_SIZE][GRID_SIZE];
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
        Random random = new Random();

        int[][] blockStarts = {
                {0, 0}, {0, 3},
                {2, 0}, {2, 3},
                {4, 0}, {4, 3}
        };

        for (int[] blockStart : blockStarts) {
            int startRow = blockStart[0];
            int startCol = blockStart[1];

            Set<Integer> usedNumbers = new HashSet<>();
            List<int[]> availablePositions = new ArrayList<>();

            availablePositions.add(new int[]{startRow, startCol});
            availablePositions.add(new int[]{startRow, startCol + 1});
            availablePositions.add(new int[]{startRow, startCol + 2});
            availablePositions.add(new int[]{startRow + 1, startCol});
            availablePositions.add(new int[]{startRow + 1, startCol + 1});
            availablePositions.add(new int[]{startRow + 1, startCol + 2});

            for (int count = 0; count < 2; count++) {
                int attempts = 0;
                boolean numberPlaced = false;

                while (!numberPlaced && attempts < 50) {
                    if (availablePositions.isEmpty()) break;

                    int posIndex = random.nextInt(availablePositions.size());
                    int[] position = availablePositions.get(posIndex);
                    int row = position[0];
                    int col = position[1];

                    int number = random.nextInt(6) + 1;

                    if (!usedNumbers.contains(number) && isValidPlacement(row, col, number)) {
                        grid[row][col] = number;
                        initialCells[row][col] = true;
                        usedNumbers.add(number);
                        availablePositions.remove(posIndex);
                        numberPlaced = true;
                    }
                    attempts++;
                }
            }
        }
    }

    public boolean isValidPlacement(int row, int col, int number) {
        if (number < MIN_VALUE || number > MAX_VALUE) {
            return false;
        }

        int originalValue = grid[row][col];//guarda el valor original, si tiene valor es porque se le cambio el valor inicial
        grid[row][col] = 0;

        //para que no se repita un valoe inicial en la fila que se va a colocar otro
        for (int c = 0; c < GRID_SIZE; c++) {
            if (grid[row][c] == number) {
                grid[row][col] = originalValue;
                return false;
            }
        }


        for (int r = 0; r < GRID_SIZE; r++) {
            if (grid[r][col] == number) {
                grid[row][col] = originalValue;
                return false;
            }
        }

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

        if (value < MIN_VALUE || value > MAX_VALUE) {//programacion defensiva, redundante
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
                {0, 0}, {0, 3},  // Bloques superiores
                {2, 0}, {2, 3},  // Bloques medios
                {4, 0}, {4, 3}   // Bloques inferiores
        };

        for (int[] blockStart : blockStarts) {
            int startRow = blockStart[0];
            int startCol = blockStart[1];

            Set<Integer> seen = new HashSet<>();
            List<int[]> positions = new ArrayList<>();

            // Recolectar valores del bloque 3x2
            for (int r = startRow; r < startRow + 2; r++) {
                for (int c = startCol; c < startCol + 3; c++) {
                    int value = grid[r][c];
                    if (value != 0) {
                        positions.add(new int[]{r, c, value});
                    }
                }
            }

            // Buscar duplicados en el bloque
            for (int[] pos : positions) {
                int value = pos[2];
                if (seen.contains(value)) {
                    // Marcar TODAS las celdas con este valor en el bloque
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