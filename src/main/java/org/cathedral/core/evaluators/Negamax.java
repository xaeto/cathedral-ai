package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedrale.heuristics.Heuristic;
import org.cathedrale.heuristics.HeuristicsHelper;
import org.cathedrale.heuristics.ZoneHeuristic;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Negamax extends Evaluator {
    public Negamax(Heuristic... heuristics) {
        super(heuristics);
    }

    private List<Placement> generateInitialKillerMoves(Game game, int depth) {
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);

        // Evaluate all possible moves and sort them based on the evaluation
        possiblePlacements.sort((move1, move2) -> {
            game.takeTurn(move1, false);
            double eval1 = -negamax(game, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
            game.undoLastTurn();

            game.takeTurn(move2, false);
            double eval2 = -negamax(game, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
            game.undoLastTurn();

            return Double.compare(eval2, eval1); // Descending order
        });

        // Return the top N moves as initial killer moves
        int numKillerMoves = Math.min(2, possiblePlacements.size()); // You can adjust this value
        return possiblePlacements.subList(0, numKillerMoves);
    }

    private double negamax(Game game, int depth, double alpha, double beta, boolean allowNullMove){
        if(depth <= 0 || game.isFinished()){
            return Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, 1) * c.getWeight()).sum();
        }

        if (allowNullMove) {
            game.forfeitTurn();
            // Make null move
            double nullMoveScore = -negamax(game, depth - 2, -beta, -alpha, false);
            game.undoLastTurn();

            // Check if null move pruning is applicable
            if (nullMoveScore >= beta) {
                return beta; // Beta cutoff
            }
        }

        double max = Double.NEGATIVE_INFINITY;
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
        possiblePlacements.sort(Comparator.comparingInt(placement -> -placement.building().score()));
        for(Placement placement : possiblePlacements){
            game.takeTurn(placement, false);
            double score = -negamax(game, depth -1, -beta, -alpha, true);
            game.undoLastTurn();

            if (score >= beta) {
                return score; // Beta cutoff
            }

            if (score > max) {
                max = score;
            }

            alpha = Math.max(alpha, score);
        }

        return max;
    }

    @Override
    public Placement eval(Game game) {
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
        Placement best = null;

        double alpha = -10000;
        double beta = 10000;

        for (Placement placement : possiblePlacements) {
            game.takeTurn(placement, false);
            double eval = -negamax(game, DEPTH - 1, -beta, -alpha, false);
            game.undoLastTurn();
            if (eval >= alpha) {
                alpha = eval;
                best = placement;
            }

            beta = Math.min(beta, eval);  // Update beta
        }

        return best;
    }
}