package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

import java.util.ArrayList;
import java.util.List;

public class BlockNewHeuristic extends Heuristic {
    public BlockNewHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        var turn = game.lastTurn().copy().getAction();
        var gameCopy = game.copy();

        gameCopy.undoLastTurn();
        gameCopy.forfeitTurn();
        double previousEnemyZoneScore = HeuristicsHelper.countFieldById(gameCopy.getBoard(), gameCopy.getCurrentPlayer().subColor());

        var possibleEnemyPlacements = HeuristicsHelper.getPossiblePlacements(gameCopy);
        List<PlacementScore> zoneCreatingPlacements = new ArrayList<>();

        for(var enemyPlacement : possibleEnemyPlacements){
            gameCopy.takeTurn(enemyPlacement, false);
            double score = HeuristicsHelper.countFieldById(gameCopy.getBoard(), gameCopy.getCurrentPlayer().opponent().subColor());
            if(score > previousEnemyZoneScore){
                var scoredPlacement = new PlacementScore(enemyPlacement, score);
                zoneCreatingPlacements.add(scoredPlacement);
            }
            gameCopy.undoLastTurn();
        }

        // forfeit enemy turn
        gameCopy.forfeitTurn();
        gameCopy.takeTurn(turn, false);

        double avoidedEnemyScore = 0;
        for(var zoneCreatingPlacement : zoneCreatingPlacements){
            if(gameCopy.takeTurn(zoneCreatingPlacement.placement(), false)){
                gameCopy.undoLastTurn();
            } else {
                // avoided turn
                avoidedEnemyScore += 1;
            }
        }

        return avoidedEnemyScore;
    }
}
