package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.example.NeuralNetwork;

public class ZoneHeuristic extends Heuristic {
    public ZoneHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game, int depth) {
        Placement placement = game.lastTurn().copy().getAction();
        double after = HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().opponent().subColor());
        Game previousState = game.copy();
        previousState.undoLastTurn();
        double previous = HeuristicsHelper.countFieldById(previousState.getBoard(), previousState.getCurrentPlayer().subColor());
        double diff = previous - after;
        if(diff == 0){
            return -0.5;
        }
        if(diff <= 3){
            return -1;
        }
        return diff;
    }
}
