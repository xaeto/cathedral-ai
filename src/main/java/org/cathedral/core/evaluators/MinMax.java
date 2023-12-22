package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedrale.heuristics.Heuristic;
import org.cathedrale.heuristics.HeuristicsHelper;

import java.util.Arrays;
import java.util.List;

public class MinMax extends Evaluator {
    public MinMax(Heuristic... heuristics) {
        super(heuristics);
    }

    private double miniMax(Game game, int depth, boolean maximize){
        if(depth == 0 || game.isFinished()){
            return Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, 1) * c.getWeight()).sum();
        }

        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
        if(maximize){
            double maxEval = Double.NEGATIVE_INFINITY;
            for(Placement placement : possiblePlacements){
                game.takeTurn(placement, false);
                double eval = miniMax(game, depth -1, false);
                game.undoLastTurn();
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for(Placement placement : possiblePlacements){
                game.takeTurn(placement, false);
                double eval = miniMax(game, depth -1, true);
                game.undoLastTurn();
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }

    @Override
    public Placement eval(Game game) {
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
        double score = Double.NEGATIVE_INFINITY;
        Placement best = null;

        for(Placement placement : possiblePlacements){
            game.takeTurn(placement, false);
            double eval = miniMax(game, DEPTH - 1, false);
            game.undoLastTurn();
            if(eval >= score){
                score = eval;
                best = placement;
            }
        }

        return best;
    }
}
