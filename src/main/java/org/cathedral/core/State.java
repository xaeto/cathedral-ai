package org.cathedral.core;

public class State {
    private int[][] board;
    private final int NUM_BUILDINGS = 10;
    public double[] flattenBoard(){
        int rows = board.length;
        int cols = board[0].length;

        double[] flattened = new double[rows * cols * NUM_BUILDINGS]; // NUM_BUILDINGS is the number of unique building types
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int buildingId = board[i][j];
                int index = i * cols * NUM_BUILDINGS + j * NUM_BUILDINGS + buildingId;
                flattened[index] = 1.0;  // One-hot encoding
            }
        }

        return flattened;
    }
}
