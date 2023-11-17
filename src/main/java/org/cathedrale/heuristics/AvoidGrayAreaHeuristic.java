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
            var field = game.getBoard().getField();
            if(field[action.y()][action.x()] == game.getCurrentPlayer().opponent().subColor()){
                return action.building().score();
            }
            game.takeTurn(action, false);
        }

        return 0;
    }
}
