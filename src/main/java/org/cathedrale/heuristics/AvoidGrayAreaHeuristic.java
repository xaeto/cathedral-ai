package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class AvoidGrayAreaHeuristic extends Heuristic {
    public AvoidGrayAreaHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        if(game.lastTurn().hasAction()){
            var action = game.lastTurn().copy().getAction();
            game.undoLastTurn();
            int x = action.x();
            int y = action.y();

            var field = game.getBoard().getField();

            boolean isGray = false;
            for(var pos : action.form()){
                int dx = x + pos.x();
                int dy = y + pos.y();

                if(field[dy][dx] == game.getCurrentPlayer().subColor()){
                    isGray = true;
                    break;
                }
            }

            game.takeTurn(action, false);
            if(isGray){
                return -5;
            }
        }

        return 0;
    }
}
