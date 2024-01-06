package org.cathedral.heuristics;

import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Game;

public class LargeBuildingHeuristic extends Heuristic {
    public LargeBuildingHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        if (game.lastTurn().hasAction()) {
            Building lastBuilding = game.lastTurn().getAction().building();

            if (lastBuilding != Building.Blue_Cathedral) {
                int buildingSize = game.lastTurn().getAction().form().size();
                double score = buildingSize;

                return score;
            }
        }
        return 0;
    }
}
