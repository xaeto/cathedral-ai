package org.cathedral.core;

public class HashEntry {
    private double score;
    private int depth;
    private HashEntryType type;

    public HashEntry(double score, int depth, HashEntryType type){
        this.score = score;
        this.depth = depth;
        this.type = type;
    }

    public double getScore(){
        return this.score;
    }
}
