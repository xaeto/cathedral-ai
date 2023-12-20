package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;

public class AvoidGrayAreaHeuristic extends Heuristic {
    public AvoidGrayAreaHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        if(game.lastTurn().hasAction()){
            var action = game.lastTurn().copy().getAction();
            game.undoLastTurn();
            int x = action.x();
            int y = action.y();

            var field = game.getBoard().getField();

            int countInGray = 0;
            for(var pos : action.form()){
                int dx = x + pos.x();
                int dy = y + pos.y();

                if(field[dy][dx] != Color.None){
                    countInGray--;
                }
            }

            game.takeTurn(action, false);
            return countInGray;
        }

        return 0;
    }
}
