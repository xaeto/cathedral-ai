package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class GameScoreHeuristic extends Heuristic {
    public GameScoreHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        double diff = game.score().getOrDefault(game.getCurrentPlayer(), 0)
                - game.score().getOrDefault(game.getCurrentPlayer().opponent(), 0);
        return diff;
    }
}
