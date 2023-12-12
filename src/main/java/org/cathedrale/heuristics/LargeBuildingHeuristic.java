package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class LargeBuildingHeuristic extends Heuristic {
    public LargeBuildingHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        if(game.lastTurn().hasAction()){
            var score = game.lastTurn().getAction().building().score();
            return score;
        }
        return 0;
    }
}
