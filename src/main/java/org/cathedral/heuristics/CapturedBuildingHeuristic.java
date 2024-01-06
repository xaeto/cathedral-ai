package org.cathedral.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class CapturedBuildingHeuristic extends Heuristic {
    public CapturedBuildingHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        var turn = game.lastTurn().copy().getAction();
        game.undoLastTurn();

        var previousEnemyPlacements = game.getBoard().getPlacedBuildings();
        for(var enemyPlacements : previousEnemyPlacements){
        }
        return 0;
    }
}
