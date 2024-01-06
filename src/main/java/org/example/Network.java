package org.example;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;

public class Network {
    private static final double discountFactor = 0.95;
    private static final double epsilon = 0.5;
    private static final double epsilonDecayFactor = 0.999;
    private static final double numEpisodes = 500;

    public static int getFieldCountByColor(Board board, Color color){
        var field = board.getField();
        int count = 0;
        for(int y = 0; y < 10; ++y){
            for(int x = 0; x < 10; ++x){
                if(field[y][x] == color)
                    count++;
            }
        }

        return count;
    }

    public static void printMatrix(Color[][] matrix){
        for(int i = 0; i < matrix.length; ++i){
            printRow(matrix[i]);
            System.out.println("\n");
        }
        System.out.println();
    }

    private static void printRow(Color[] row){
        for(int i = 0; i < row.length; ++i){
            System.out.print(row[i].getId() + " ");
        }
    }
}
