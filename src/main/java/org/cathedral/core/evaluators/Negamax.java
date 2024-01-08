package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.*;
import org.cathedral.heuristics.Heuristic;
import org.cathedral.heuristics.HeuristicsHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Negamax extends Evaluator {

    public Negamax(Heuristic... heuristics) {
        super(heuristics);
    }

    private double negamax(Game game, int depth, double alpha, double beta){
        if(depth <= 0 || game.isFinished()){
            this.total.incrementAndGet();
            return Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, 1) * c.getWeight()).sum();
        }

        double score = Double.NEGATIVE_INFINITY;
        var moves = HeuristicsHelper.getPossiblePlacements(game);
        moves.sort(Comparator.comparingDouble(placement -> {
            game.takeTurn(placement, false);
            double s = Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, 1) * c.getWeight()).sum();
            game.undoLastTurn();
            return -s;
        }));
        for(var move : moves){
            game.takeTurn(move, false);
            double eval = -negamax(game, depth -1, -beta, -alpha);
            if(eval > score){
                score = eval;
            }
            if(score > alpha){
                alpha = score;
            }
            game.undoLastTurn();
            if(alpha >= beta){
                cut.incrementAndGet();
                return alpha;
            }
        }
        return score;
    }

    @Override
    public Placement eval(Game game) {
        Placement best = null;
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        for (Placement move : possiblePlacements) {
            game.takeTurn(move, false);
            double eval = -negamax(game, DEPTH - 1, -beta, -alpha);
            // System.out.println("Eval: " + eval);
            game.undoLastTurn();

            if (eval >= alpha) {
                alpha = eval;
                best = move;
            }
            beta = Math.max(eval, beta);
        }

        printStats();
        resetStats();

        return best;
    }
}
