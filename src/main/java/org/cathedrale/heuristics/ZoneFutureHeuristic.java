package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class ZoneFutureHeuristic extends Heuristic {
    public ZoneFutureHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        var cp = game.copy();
        cp.forfeitTurn();
        return 0;
    }
}
