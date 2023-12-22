package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Game;

public class LargeBuildingHeuristic extends Heuristic {
    public LargeBuildingHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        if(game.lastTurn().hasAction()){
            if(game.lastTurn().getAction().building() != Building.Blue_Cathedral){
                var score = game.lastTurn().getAction().form().size();
                return score;
            }
        }
        return 0;
    }
}
