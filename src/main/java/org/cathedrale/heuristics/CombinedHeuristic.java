package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class CombinedHeuristic extends Heuristic {
    private Heuristic[] heuristics;

    public CombinedHeuristic(double weight, Heuristic[] heuristics){
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        return 0;
    }
}
