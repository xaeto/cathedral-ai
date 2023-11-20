package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class GameStateHeuristic extends Heuristic {

    public GameStateHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        var buildings = game.getPlacableBuildings();
        boolean lost = false;
        for(var building : buildings){
            for(var placement : building.getPossiblePlacements(game)){
                game.takeTurn(placement, false);
                if(game.isFinished()){
                    lost = true;
                }
                game.undoLastTurn();
                if(lost)
                    break;
            }
            if(lost){
                break;
            }
        }
        return lost ? -1000 : 0;
    }
}
