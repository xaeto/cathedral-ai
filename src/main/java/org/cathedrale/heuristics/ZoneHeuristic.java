package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class ZoneHeuristic extends Heuristic {
    public ZoneHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        var placement = game.lastTurn().copy();
        game.undoLastTurn();
        double previous = HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().subColor());
        game.takeTurn(placement.getAction());
        double after = HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().opponent().subColor());

        double size = after - previous;
        return after > previous + 3 ? size : 0;
    }
}
