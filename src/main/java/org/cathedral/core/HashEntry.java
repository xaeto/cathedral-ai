package org.cathedral.core;

import java.io.Serializable;

public class HashEntry implements Serializable {
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
    public int getDepth(){
        return this.depth;
    }
    public HashEntryType getType(){
        return this.type;
    }
}
