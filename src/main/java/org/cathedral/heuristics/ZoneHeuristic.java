package org.cathedral.heuristics;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.board.fast.BitBoard;

public class ZoneHeuristic extends Heuristic {
    public ZoneHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        double after = HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().opponent().subColor());
        Placement placement = game.lastTurn().copy().getAction();
        game.undoLastTurn();
        double previous = HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().subColor());
        game.takeTurn(placement, false);
        return after - previous;
    }

    public double eval(BitBoard board, int depth) {
        return 1;
    }
}
