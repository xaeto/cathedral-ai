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
        Placement turn = game.lastTurn().copy().getAction();
        game.undoLastTurn();
        double previous = HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().subColor());
        game.takeTurn(turn, false);

        double after = HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().opponent().subColor());
        double diff = after - previous;

        if(diff < 3){
            diff = 0;
        }
        return diff * (depth + 1) / 13;
    }
}
