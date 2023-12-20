package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;

import java.util.ArrayList;

public class BlockNewHeuristic extends Heuristic {
    public BlockNewHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        // our turn has already been done
        var cp = game.copy();
        Placement ourTurn = game.lastTurn().copy().getAction();
        cp.undoLastTurn();

        // forfeit our turn
        cp.forfeitTurn();

        var enemyPlacements = HeuristicsHelper.getPossiblePlacements(cp);
        double previousEnemyScore = HeuristicsHelper.countFieldById(cp.getBoard(), cp.getCurrentPlayer());

        var enemyPlacementsToBlock = new ArrayList<Placement>();

        for(var enemyPlacement : enemyPlacements){
            cp.takeTurn(enemyPlacement, false);
            double eval = HeuristicsHelper.countFieldById(cp.getBoard(), cp.getCurrentPlayer().opponent());
            double diff = previousEnemyScore - eval;

            if(diff >= 3){
                enemyPlacementsToBlock.add(enemyPlacement);
            }
            cp.undoLastTurn();
        }

        if(enemyPlacementsToBlock.isEmpty()){
            return -1;
        }

        cp.forfeitTurn();
        cp.takeTurn(ourTurn, false);

        double blocked = 0;
        for(var enemyPlacement : enemyPlacementsToBlock){
            boolean take = cp.takeTurn(enemyPlacement, false);
            if(!take){
                cp.undoLastTurn();
                cp.forfeitTurn();
                cp.takeTurn(enemyPlacement, false);

                double eval = HeuristicsHelper.countFieldById(cp.getBoard(), cp.getCurrentPlayer().opponent());
                double diff = previousEnemyScore - eval;
                blocked += diff;

                cp.undoLastTurn();
            } else {
                cp.undoLastTurn();
            }
        }

        return blocked;
    }
}
