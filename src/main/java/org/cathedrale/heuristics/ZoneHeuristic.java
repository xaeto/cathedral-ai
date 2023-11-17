package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class ZoneHeuristic extends Heuristic {
    public ZoneHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        if(game.lastTurn().hasAction()){
            var lastTurn = game.lastTurn().copy();
            game.undoLastTurn();

            double score = countFieldsByPlayerId(game.getBoard(), game.getCurrentPlayer().opponent().subColor());
            game.takeTurn(lastTurn.getAction(), false);

            double nextScore = countFieldsByPlayerId(game.getBoard(), game.getCurrentPlayer().opponent().subColor());
            game.takeTurn(lastTurn.getAction(), false);

            double res = 0;
            if((nextScore - score) == 1){
                res = -1.0;
            }
            if((nextScore - score) > 1){
                res = -0.5;
            }
            if((nextScore - score) > 4){
                res = 1.0;
            }
            if((nextScore - score) >= 8){
                res = 5.0;
            }
            if((nextScore - score) >= 10){
                res = 50;
            }
            return res;
        }

        return 0;
    }
}
