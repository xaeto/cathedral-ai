package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;
import org.cathedrale.heuristics.HeuristicsHelper;

public class PlayerAreaHeuristic extends Heuristic {
    public PlayerAreaHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        return HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer());
    }
}
