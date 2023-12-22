package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedrale.heuristics.Heuristic;
import org.cathedrale.heuristics.HeuristicsHelper;

import java.util.Arrays;
import java.util.List;

public class AlphaBetaNullMove extends Evaluator {
    public AlphaBetaNullMove(Heuristic[] heuristics) {
        super(heuristics);
    }

    private double alphaBeta(Game game, int depth, double alpha, double beta, boolean maximize){
        if(depth == 0 || game.isFinished()){
            this.total++;
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
                        this.cut++;
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
                        this.cut++;
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
        for(Placement placement : possiblePlacements){
            if(game.takeTurn(placement, false)){
                double eval = Math.max(score, alphaBeta(game, DEPTH - 1, Double.NEGATIVE_INFINITY, Double. POSITIVE_INFINITY, false));
                if(eval >= score){
                    score = eval;
                    best = placement;
                }
                game.undoLastTurn();
            }
        }

        double optimal = alphaBeta(game, DEPTH, Double.NEGATIVE_INFINITY, Double. POSITIVE_INFINITY, true);
        System.out.println("Optimal Score: " + optimal);

        printStats();
        resetStats();
        return best;
    }
}
