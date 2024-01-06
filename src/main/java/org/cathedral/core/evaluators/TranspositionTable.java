package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import org.cathedral.core.HashEntry;
import org.cathedral.core.HashEntryType;
import org.opencv.aruco.Board;
import org.work.Zobrist;

import java.util.HashMap;

public class TranspositionTable {
    private HashMap<Long, HashEntry> transpositionTable = new HashMap<>();
    public HashEntry get(Game game){
        long hash = Zobrist.hashify(game);
        return transpositionTable.get(hash);
    }

    public void add(long hash, double score, int depth, HashEntryType type){
        HashEntry entry = new HashEntry(score, depth, type);

        this.transpositionTable.putIfAbsent(hash, entry);
    }

    public void updateEntry(long hash, double score, int depth, HashEntryType type) {
        HashEntry existingEntry = transpositionTable.get(hash);

        if (existingEntry == null || depth >= existingEntry.getDepth()) {
            // Update or add entry if the new information is more relevant
            HashEntry newEntry = new HashEntry(score, depth, type);
            this.transpositionTable.put(hash, newEntry);
        }
    }

    public void reset(){
        this.transpositionTable.clear();
    }
}
