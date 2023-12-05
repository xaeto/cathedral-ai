package org.work;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Placement;

import java.util.Arrays;
import java.util.Random;

public class Zobrist {
    private static final int BOARD_SIZE = 10; // Beispiel: Spielfeldgröße 10x10
    private static final int NUM_PIECES = 23; // Beispiel: 6 verschiedene Figurentypen in 2 Farben

    private static long[][][] zobristKeys = new long[BOARD_SIZE][BOARD_SIZE][NUM_PIECES];

    // Initialisiere die Zobrist-Hash-Keys mit zufälligen Werten
    static {
        Random random = new Random();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                for (int k = 0; k < NUM_PIECES; k++) {
                    zobristKeys[i][j][k] = random.nextLong();
                }
            }
        }
    }

    private long hash;
    public Zobrist(long hash){
        this.hash = hash;
    }

    public Zobrist clone(){
        return new Zobrist(this.hash);
    }

    public void update(Board board, Placement placement){
        var field = board.getField();
        int x = placement.x();
        int y = placement.y();

        int id = placement.building().getId();
        for(var pos : placement.form()){
            int dx = pos.x() + x;
            int dy = pos.y() + y;
            this.hash ^= field[dy][dx].getId();
        }
    }
    public static long hashify(Board board) {
        var field = board.getField();
        long hash = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int piece = field[i][j].getId();
                if (piece != 0) {
                    // XOR des Zobrist-Keys für das Stück an Position (i, j) im Spielfeld
                    hash ^= zobristKeys[i][j][piece - 1];
                }
            }
        }
        return hash;
    }
    public void update(Board board){
        var field = Arrays.stream(board.getField()).flatMap(Arrays::stream).mapToInt(c-> c.getId()).toArray();
        for(int i = 0; i < 10*10;++i){
            hash ^= field[i];
        }
    }
}
