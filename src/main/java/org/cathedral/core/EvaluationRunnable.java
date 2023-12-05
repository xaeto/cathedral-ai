package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Game;
import org.cathedrale.heuristics.Heuristic;

public class EvaluationRunnable implements Runnable {
    private final Heuristic[] heuristics;
    private final Game game;

    public EvaluationRunnable(Game game, Heuristic[] heuristics){
        this.heuristics = heuristics;
        this.game = game;
    }

    @Override
    public void run() {
    }
}
