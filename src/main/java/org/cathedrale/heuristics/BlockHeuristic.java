package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class BlockHeuristic extends Heuristic{
    public BlockHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        if(game.lastTurn().hasAction()){
            var placement = game.lastTurn().copy().getAction();
            game.undoLastTurn();

            double playerScore = countFieldsByPlayerId(game.getBoard(), game.getCurrentPlayer().subColor());
            game.takeTurn(placement, false);
            double enemyScore = countFieldsByPlayerId(game.getBoard(), game.getCurrentPlayer().opponent().subColor());

            if(playerScore > enemyScore + 3){
                return 1;
            }
        }

        return 0;
    }
}
