package org.cathedral.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class BuildingHeuristic extends Heuristic {
    public BuildingHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(final Game game, int depth) {
        if(game.lastTurn().hasAction()){
            return game.lastTurn().getAction().building().score();
        }
        return 0;
    }
}
