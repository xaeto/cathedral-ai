package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class GameStateHeuristic extends Heuristic {

    public GameStateHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        var turn = game.lastTurn().copy().getAction();
        game.undoLastTurn();
        return 0;
    }
}
