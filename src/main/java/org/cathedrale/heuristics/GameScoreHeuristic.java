package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class GameScoreHeuristic extends Heuristic {
    public GameScoreHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        double player = game.score().getOrDefault(game.getCurrentPlayer().opponent(), 0);
        double enemy = game.score().getOrDefault(game.getCurrentPlayer(), 0);

        boolean better = enemy > player;
        return better ? 47 - player : -(47 - enemy);
    }
}
