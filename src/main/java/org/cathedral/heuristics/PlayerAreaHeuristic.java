package org.cathedral.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class PlayerAreaHeuristic extends Heuristic {
    public PlayerAreaHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        return HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer()) * (game.lastTurn().hasAction() ? game.lastTurn().getAction().building().score() : 1);
    }
}
