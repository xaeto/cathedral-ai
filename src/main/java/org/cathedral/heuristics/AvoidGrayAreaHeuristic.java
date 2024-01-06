package org.cathedral.heuristics;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;

public class AvoidGrayAreaHeuristic extends Heuristic {
    public AvoidGrayAreaHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        if(game.lastTurn().hasAction()){
            Placement lastTurn = game.lastTurn().copy().getAction();
            var cp = game.copy();

            int x = lastTurn.x();
            int y = lastTurn.y();

            cp.undoLastTurn();
            boolean placedInZone = cp.getBoard().getField()[y][x] == game.getCurrentPlayer().opponent().subColor();
            return placedInZone ? -1 : 0;
        }

        return 0;
    }
}
