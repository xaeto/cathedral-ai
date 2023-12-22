package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedrale.heuristics.Heuristic;
import org.cathedrale.heuristics.HeuristicsHelper;
import org.cathedrale.heuristics.PlacementScore;

import java.util.Arrays;
import java.util.List;

public abstract class Evaluator {
    protected Heuristic[] heuristics;
    protected final static int DEPTH = 2;
    protected int cut = 0;
    protected int total = 0;
    public Evaluator(Heuristic... heuristics){
        this.heuristics = heuristics;
    }

    public abstract Placement eval(Game game);
    protected void resetStats(){
        this.cut = 0;
        this.total = 0;
    }

    protected void printStats(){
        System.out.println("Boards evaluated: " + total);
    }
}
