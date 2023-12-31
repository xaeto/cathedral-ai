package org.cathedral.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class GameScoreHeuristic extends Heuristic {
    public GameScoreHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        double player = game.score().getOrDefault(game.getCurrentPlayer().opponent(), 47);
        double enemy = game.score().getOrDefault(game.getCurrentPlayer(), 47);
        return player - enemy;
    }
}
