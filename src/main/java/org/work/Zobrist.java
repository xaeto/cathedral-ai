package org.work;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedral.core.HashEntry;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Zobrist {
    private static final int BOARD_SIZE = 100; // Beispiel: Spielfeldgröße 10x10
    private static long[][] zobristTable = new long[BOARD_SIZE][23];

    // Initialisiere die Zobrist-Hash-Keys mit zufälligen Werten
    static {
        Random random = new Random();
        random.setSeed(1);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 23; j++) {
                zobristTable[i][j] = random.nextLong();
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
    public static long hashify(final Game game) {
        var field = game.getBoard().getField();
        long hash = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int pieceType = field[i][j].getId();
                hash ^= zobristTable[i * 10 + j][pieceType];
            }
        }
        return hash;
    }

    public static void save(HashMap<Long, HashEntry> transpositionTable) {
        FileOutputStream f = null;
        ObjectOutputStream o = null;
        try {
            f = new FileOutputStream(new File("zobrist.bin"));
            o = new ObjectOutputStream(f);
            o.writeObject(transpositionTable);

            o.close();
            f.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<Long, HashEntry> load(){
        FileInputStream fi = null;
        ObjectInputStream oi = null;
        try {
            fi = new FileInputStream(new File("zobrist.bin"));
            oi = new ObjectInputStream(fi);

            oi.close();
            fi.close();
            return (HashMap<Long, HashEntry>) oi.readObject();
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        } catch (IOException e) {
            return new HashMap<>();
        } catch (ClassNotFoundException e) {
            return new HashMap<>();
        }
    }
}
