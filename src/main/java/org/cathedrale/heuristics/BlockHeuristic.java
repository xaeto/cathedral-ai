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

            double score = countFieldsByPlayerId(game.getBoard(), game.getCurrentPlayer().subColor());
            game.takeTurn(placement, false);
            double nextScore = countFieldsByPlayerId(game.getBoard(), game.getCurrentPlayer().opponent().subColor());

            if(score > nextScore){
                return -(nextScore - score);
            }

            // blocked Score
            if(score == nextScore){
                return 10;
            }

            if(nextScore < score){
                return 100*(score + nextScore);
            }
        }

        return 0;
    }
}
