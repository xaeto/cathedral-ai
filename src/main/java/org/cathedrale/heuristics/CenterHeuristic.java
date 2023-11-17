package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class CenterHeuristic extends Heuristic {
    public CenterHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        return 0;
    }
}