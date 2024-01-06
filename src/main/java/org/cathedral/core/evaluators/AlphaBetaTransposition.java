package org.cathedral.core.evaluators;

import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedral.core.HashEntry;
import org.cathedral.core.HashEntryType;
import org.cathedral.heuristics.Heuristic;
import org.cathedral.heuristics.HeuristicsHelper;
import org.work.Zobrist;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AlphaBetaTransposition extends Evaluator {
    private ConcurrentHashMap<Long, HashEntry> transpositionTableMax = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, HashEntry> transpositionTableMin = new ConcurrentHashMap<>();
    public AlphaBetaTransposition(Heuristic[] heuristics) {
        super(heuristics);
    }

    private double alphaBeta(Game game, int depth, double alpha, double beta, boolean maximize){
        long hash = Zobrist.hashify(game);
        HashEntry entry = maximize ? transpositionTableMax.get(hash) : transpositionTableMin.get(hash);
        if(maximize){
            if (entry != null && entry.getDepth() >= depth) {
                this.total.incrementAndGet();
                return entry.getScore();
            }
        }

        if(depth == 0 || game.isFinished()){
            this.total.incrementAndGet();
            double score = Arrays.stream(this.heuristics).mapToDouble(c -> c.eval(game, 1) * c.getWeight()).sum();
            entry = new HashEntry(score, depth, HashEntryType.EXACT);
            if(maximize){
                this.transpositionTableMax.putIfAbsent(hash, entry);
            } else {
                this.transpositionTableMin.putIfAbsent(hash, entry);
            }
            return score;
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
            entry = new HashEntry(maxEval, depth, HashEntryType.LOWER);
            this.transpositionTableMax.putIfAbsent(hash, entry);
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
            entry = new HashEntry(maxEval, depth, HashEntryType.UPPER);
            this.transpositionTableMin.putIfAbsent(hash, entry);
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
                double eval = alphaBeta(game, DEPTH - 1, Double.NEGATIVE_INFINITY, Double. POSITIVE_INFINITY, false);
                if(eval >= score){
                    score = eval;
                    best = placement;
                }
                game.undoLastTurn();
            }
        }

        printStats();
        resetStats();
        return best;
    }
}
