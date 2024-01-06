package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedral.heuristics.Heuristic;
import org.cathedral.heuristics.HeuristicsHelper;

import java.util.Arrays;
import java.util.List;

public class NegaScout extends Evaluator {

    public NegaScout(Heuristic... heuristics) {
        super(heuristics);
    }

    private double negaScout(Game game, int depth, double alpha, double beta){
        if(depth <= 0 || game.isFinished()){
            this.total.incrementAndGet();
            return Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, 1) * c.getWeight()).sum();
        }

        double score = Double.NEGATIVE_INFINITY;
        double n = beta;
        var moves = HeuristicsHelper.getPossiblePlacements(game);
        for(var move : moves){
            game.takeTurn(move, false);
            double current = -negaScout(game, depth -1, -n, -alpha);
            if(current > score){
                if(n == beta || depth <= 2){
                    score = current;
                } else {
                    score = -negaScout(game, depth -1, -beta, -current);
                }
            }
            game.undoLastTurn();
            if(score > alpha){
                alpha = score;
            }
            if(alpha >= beta){
                cut.incrementAndGet();
                return alpha;
            }
            n = alpha + 1;
        }

        return score;
    }

    @Override
    public Placement eval(Game game) {
        Placement best = null;
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);

        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;
        for(Placement placement : possiblePlacements){
            game.takeTurn(placement, false);
            double eval = negaScout(game, DEPTH, -beta, -alpha);
            game.undoLastTurn();
            if(eval >= alpha){
                alpha = eval;
                best = placement;
            }

            // beta = Math.min(beta, eval);
        }

        printStats();
        resetStats();

        return best;
    }
}
