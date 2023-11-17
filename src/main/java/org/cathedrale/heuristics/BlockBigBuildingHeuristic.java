package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Game;

import java.util.Comparator;

public class BlockBigBuildingHeuristic extends Heuristic {

    public BlockBigBuildingHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        if(!game.lastTurn().hasAction())
            return 0;

        var action = game.lastTurn().copy().getAction();
        game.undoLastTurn();
        double result = 0;
        var max = game.getPlacableBuildings(game.getCurrentPlayer())
                .stream().max(Comparator.comparingDouble(Building::score));

        game.takeTurn(action, false);
        if(max.isPresent()){
            for(var maxPos : max.get().getAllPossiblePlacements()){
                if(!game.takeTurn(maxPos, false)){
                    result += 1;
                } else {
                    result -= 1;
                    game.undoLastTurn();
                }
            }
        }
        return result;
    }
}
