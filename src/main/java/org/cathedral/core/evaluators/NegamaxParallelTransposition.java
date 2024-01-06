package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedral.core.HashEntry;
import org.cathedral.core.HashEntryType;
import org.cathedral.heuristics.Heuristic;
import org.cathedral.heuristics.HeuristicsHelper;
import org.nd4j.common.primitives.AtomicDouble;
import org.work.Zobrist;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class NegamaxParallelTransposition extends Evaluator {
    private TranspositionTable transpositionTable = new TranspositionTable();
    public NegamaxParallelTransposition(Heuristic... heuristics) {
        super(heuristics);
    }

    private double negamax(Game game, int depth, double alpha, double beta, boolean allowNullMove){
        if(depth == 0 || game.isFinished()){
            long hash = Zobrist.hashify(game);
            double score = Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, depth) * c.getWeight()).sum();
            transpositionTable.add(hash, score, depth, HashEntryType.EXACT);
            this.total.incrementAndGet();
            return score;
        }

        long hash = Zobrist.hashify(game);

        HashEntry transpositionEntry = transpositionTable.get(game);
        if (transpositionEntry != null) {
            if (transpositionEntry.getDepth() == depth && transpositionEntry.getType() == HashEntryType.EXACT) {
                this.cached.incrementAndGet();
                return transpositionEntry.getScore();
            } else {
                double entryScore = transpositionEntry.getScore();

                if (transpositionEntry.getType() == HashEntryType.LOWER) {
                    alpha = Math.max(alpha, entryScore);
                } else if (transpositionEntry.getType() == HashEntryType.UPPER) {
                    beta = Math.min(beta, entryScore);
                }

                if (alpha >= beta) {
                    this.cut.incrementAndGet();
                    transpositionTable.updateEntry(hash, beta, depth, HashEntryType.UPPER);
                    return entryScore; // Beta cutoff
                }
            }
        }
        double max = Double.NEGATIVE_INFINITY;
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);
        for(Placement placement : possiblePlacements){
            game.takeTurn(placement, false);
            long shash = Zobrist.hashify(game);
            double score = -negamax(game, depth -1, -beta, -alpha, true);

            game.undoLastTurn();

            if (score >= beta) {
                transpositionTable.updateEntry(shash, beta, depth, HashEntryType.UPPER);
                this.cut.incrementAndGet();
                return score;
            }

            if (score >= max) {
                max = score;
            }

            alpha = Math.max(alpha, score);
        }
        transpositionTable.add(Zobrist.hashify(game), max, depth, HashEntryType.EXACT);
        return max;
    }

    @Override
    public Placement eval(Game game) {
        List<Placement> possiblePlacements = HeuristicsHelper.getPossiblePlacements(game);

        AtomicReference<Placement> best = new AtomicReference<>(null);

        for(int depth = 1; depth < 3; depth++){
            final int d = depth;
            AtomicDouble alpha = new AtomicDouble(Double.NEGATIVE_INFINITY);
            AtomicDouble beta = new AtomicDouble(Double.POSITIVE_INFINITY);
            System.out.println("Depth: " + d);
            possiblePlacements.parallelStream().forEach(placement -> {
                var cp = game.copy();
                cp.takeTurn(placement, false);
                double eval = -negamax(cp, d, alpha.get(), beta.get(), false);

                if (eval >= alpha.get()) {
                    alpha.set(eval);
                    best.set(placement);
                }
                beta.set(Math.min(beta.get(), eval));
            });
        }

        printStats();
        resetStats();
        transpositionTable.reset();
        return best.get();
    }
}
