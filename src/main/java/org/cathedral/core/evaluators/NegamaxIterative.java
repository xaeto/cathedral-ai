package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedrale.heuristics.Heuristic;
import org.cathedrale.heuristics.HeuristicsHelper;

import java.util.Arrays;
import java.util.List;

public class NegamaxIterative extends Evaluator {
    public NegamaxIterative(Heuristic... heuristics) {
        super(heuristics);
    }

    private double negamax(Game game, int depth, double alpha, double beta){
        if(depth == 0 || game.isFinished()){
            return Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, 1) * c.getWeight()).sum();
        }

        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
        double max = Double.NEGATIVE_INFINITY;
        for(Placement placement : possiblePlacements){
            game.takeTurn(placement, false);
            double score = -negamax(game, depth -1, -beta, -alpha);
            game.undoLastTurn();
            if(score > beta)
                return score;
            if(score > max)
                max = score;

            alpha = Math.max(alpha, score);
            if(alpha >= beta)
                break;
        }

        return max;
    }

    @Override
    public Placement eval(Game game) {
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
        double score = Double.NEGATIVE_INFINITY;
        Placement best = null;

        for(int depth = 1; depth < 6; ++depth){
            for(Placement placement : possiblePlacements){
                game.takeTurn(placement, false);
                double eval = negamax(game, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
                game.undoLastTurn();
                if(eval >= score){
                    score = eval;
                    best = placement;
                }
            }
        }

        return best;
    }
}
