package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.example.NeuralNetwork;

public class ZoneHeuristic extends Heuristic {
    public ZoneHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        double zone = HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().opponent().subColor());
        return zone < 3 ? -1 : zone;
    }
}
