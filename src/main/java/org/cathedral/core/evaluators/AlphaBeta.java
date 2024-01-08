package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedral.heuristics.Heuristic;
import org.cathedral.heuristics.HeuristicsHelper;

import java.util.Arrays;
import java.util.List;

public class AlphaBeta extends Evaluator {
    public AlphaBeta(Heuristic[] heuristics) {
        super(heuristics);
    }

    private double alphaBeta(Game game, int depth, double alpha, double beta, boolean maximize){
        if(depth == 0 || game.isFinished()){
            this.total.incrementAndGet();
            return Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, 1) * c.getWeight()).sum();
        }

        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
        double maxEval = Double.NEGATIVE_INFINITY;
        double minEval = Double.POSITIVE_INFINITY;
        if(maximize){
            for(Placement placement : possiblePlacements){
                if(game.takeTurn(placement, false)){
                    double eval = alphaBeta(game, depth - 1, alpha, beta, false);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, maxEval);
                    game.undoLastTurn();
                    if (beta <= alpha) {
                        this.cut.incrementAndGet();
                        break;
                    }
                }
            }
            return maxEval;
        } else {
            for(Placement placement : possiblePlacements){
                if(game.takeTurn(placement, false)){
                    double eval = alphaBeta(game, depth -1, alpha, beta, true);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, minEval);
                    game.undoLastTurn();
                    if(beta <= alpha){
                        this.cut.incrementAndGet();
                        break;
                    }
                }
            }
            return minEval;
        }
    }

    @Override
    public Placement eval(Game game) {
        double score = Double.NEGATIVE_INFINITY;
        Placement best = null;

        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        for(Placement placement : possiblePlacements){
            if(game.takeTurn(placement, false)){
                double eval = Math.max(score, alphaBeta(game, DEPTH - 1, alpha, beta, false));
                if(eval >= score){
                    score = eval;
                    best = placement;
                }
                game.undoLastTurn();
                beta = Math.min(beta, eval);
            }
        }

        printStats();
        resetStats();
        return best;
    }
}
