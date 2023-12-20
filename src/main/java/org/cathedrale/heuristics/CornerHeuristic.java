package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class CornerHeuristic extends Heuristic {
    public CornerHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        if(!game.lastTurn().hasAction())
            return 0;
        var placement = game.lastTurn().getAction();
        int x = placement.position().x();
        int y = placement.position().y();
        int boardSize = 10;
        double cornerBonus = 1.0;

        if ((x == 0 || x == boardSize - 1) && (y == 0 || y == boardSize - 1)) {
            return cornerBonus;
        }

        return 0.0;
    }
}
