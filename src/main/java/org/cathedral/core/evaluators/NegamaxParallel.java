package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedrale.heuristics.Heuristic;
import org.cathedrale.heuristics.HeuristicsHelper;
import org.nd4j.common.primitives.AtomicDouble;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class NegamaxParallel extends Evaluator {
    public NegamaxParallel(Heuristic... heuristics) {
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
            return Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, depth) * c.getWeight()).sum();
        }
        if (allowNullMove) {
            game.forfeitTurn();
            double nullMoveScore = -negamax(game, depth - 1, -beta, -alpha, false);
            game.undoLastTurn();

            if (nullMoveScore >= beta) {
                return beta; // Beta cutoff
            }
        }

        double max = Double.NEGATIVE_INFINITY;
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
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

        AtomicReference<Placement> best = new AtomicReference<>(null);
        AtomicDouble alpha = new AtomicDouble(Double.NEGATIVE_INFINITY);
        AtomicDouble beta = new AtomicDouble(Double.POSITIVE_INFINITY);

        possiblePlacements.parallelStream().forEach(placement -> {
            var cp = game.copy();
            cp.takeTurn(placement, false);
            double eval = -negamax(cp, DEPTH - 1, -beta.get(), -alpha.get(), false);

            if (eval >= alpha.get()) {
                alpha.set(eval);
                best.set(placement);
            }

            beta.set(Math.min(beta.get(), eval));
        });

        return best.get();
    }
}
