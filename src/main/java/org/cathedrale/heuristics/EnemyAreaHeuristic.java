package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class EnemyAreaHeuristic extends Heuristic {
    public EnemyAreaHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        return HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().opponent());
    }
}
