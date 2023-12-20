package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

import java.util.ArrayList;
import java.util.List;

public class BlockNewHeuristic extends Heuristic {
    private ZoneHeuristic zoneHeuristic = new ZoneHeuristic(1.0);
    public BlockNewHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        var turn = game.lastTurn().copy().getAction();
        var gameCopy = game.copy();

        gameCopy.undoLastTurn();
        gameCopy.forfeitTurn();
        double previousEnemyZoneScore = zoneHeuristic.eval(gameCopy, depth + 1);

        // enemy turn
        var possibleEnemyPlacements = HeuristicsHelper.getPossiblePlacements(gameCopy);
        List<PlacementScore> zoneCreatingPlacements = new ArrayList<>();

        // iterate over enemy turns
        // get all turns that generate a new zone
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
        // retake our turn
        gameCopy.takeTurn(turn, false);

        double avoidedEnemyScore = 0;
        int count = 0;
        for(var zoneCreatingPlacement : zoneCreatingPlacements){
            if(gameCopy.takeTurn(zoneCreatingPlacement.placement(), false)){
                gameCopy.undoLastTurn();
            } else {
                // avoided turn
                avoidedEnemyScore += zoneCreatingPlacement.score();
                count ++;
            }
        }

        return avoidedEnemyScore / (count + 1);
    }
}
