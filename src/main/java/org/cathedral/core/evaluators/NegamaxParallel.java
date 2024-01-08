package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedral.heuristics.Heuristic;
import org.cathedral.heuristics.HeuristicsHelper;
import org.nd4j.common.primitives.AtomicDouble;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class NegamaxParallel extends Evaluator {
    public NegamaxParallel(Heuristic... heuristics) {
        super(heuristics);
    }

    private double negamax(Game game, int depth, double alpha, double beta){
        if(depth <= 0 || game.isFinished()){
            this.total.incrementAndGet();
            return Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, 1) * c.getWeight()).sum();
        }

        double score = Double.NEGATIVE_INFINITY;
        var moves = HeuristicsHelper.getPossiblePlacements(game);
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
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);

        AtomicReference<Placement> best = new AtomicReference<>(null);
        AtomicDouble alpha = new AtomicDouble(Double.NEGATIVE_INFINITY);
        AtomicDouble beta = new AtomicDouble(Double.POSITIVE_INFINITY);

        possiblePlacements.parallelStream().forEach(placement -> {
            var cp = game.copy();
            cp.takeTurn(placement, false);
            double eval = -negamax(cp, 1, -beta.get(), -alpha.get());

            if (eval >= alpha.get()) {
                alpha.set(eval);
                best.set(placement);
            }
            beta.set(Math.min(beta.get(), eval));  // Update beta
        });

        return best.get();
    }
}
