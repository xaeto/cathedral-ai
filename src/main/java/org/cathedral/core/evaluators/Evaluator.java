package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedral.heuristics.Heuristic;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Evaluator {
    protected Heuristic[] heuristics;
    protected final static int DEPTH = 2;
    protected AtomicInteger cut = new AtomicInteger(0);
    protected AtomicInteger total = new AtomicInteger(0);
    protected AtomicInteger cached = new AtomicInteger(0);
    public Evaluator(Heuristic... heuristics){
        this.heuristics = heuristics;
    }

    public abstract Placement eval(Game game);
    protected void resetStats(){
        this.cut.set(0);
        this.total.set(0);
        this.cached.set(0);
    }

    protected void printStats(){
        System.out.println("Boards evaluated: " + total.get() + " Cut: " + cut.get() + " Cached: " + cached.get());
    }
}
