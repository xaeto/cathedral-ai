package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import io.aeron.shadow.org.HdrHistogram.DoubleLinearIterator;
import org.board.fast.FastBoard;
import org.cathedrale.heuristics.GameScoreHeuristic;
import org.cathedrale.heuristics.Heuristic;
import org.encog.util.Stopwatch;
import org.nd4j.common.primitives.AtomicDouble;
import org.work.Zobrist;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class HeuristicalAgent implements Agent {
    private static final boolean DEBUG = true;
    private Heuristic[] heuristics;
    private ConcurrentHashMap<Long, HashEntry> transpositionTable = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private Future<Double> prefetch;

    private static int minHits = 0;
    private static int maxHits = 0;
    public HeuristicalAgent(Heuristic... heuristics) {
        this.heuristics = heuristics;
    }

    @Override
    public String evaluateLastTurn(Game game) {
        return "Score: " + Heuristic.normalize(Arrays.stream(heuristics).mapToDouble(c-> c.eval(game) * c.getWeight()).sum(),
                -10000,
                10000
        );
    }

    private List<Placement> getPossiblePlacements(final Game game){
        var buildings = game.getPlacableBuildings(game.getCurrentPlayer());
        var placements = new ArrayList<Placement>();
        for(var building : buildings){
            placements.addAll(building.getPossiblePlacements(game.getBoard()));
        }

        return placements;
    }

    private List<Placement> getPossiblePlacements(Board board, Color color){
        var buildings = board.getPlacableBuildings(color);
        var placements = new ArrayList<Placement>();
        for(var building : buildings){
            placements.addAll(building.getPossiblePlacements(board));
        }

        return placements;
    }

    private double alphaBetaMin(Game game, double alpha, double beta, int depth) {
        long hash = Zobrist.hashify(game);
        HashEntry entry = transpositionTable.get(hash);
        if (entry != null && entry.getDepth() >= depth) {
            minHits++;
            return entry.getScore();
        }

        if (depth == 0 || game.isFinished()) {
            double eval = Arrays.stream(heuristics).mapToDouble(c -> c.eval(game) * c.getWeight()).sum();
            transpositionTable.put(hash, new HashEntry(eval, depth, HashEntryType.EXACT));
            return eval;
        }

        var placements = getPossiblePlacements(game);
        for (var placement : placements) {
            game.takeTurn(placement);
            double eval = alphaBetaMax(game, alpha, beta, depth - 1);
            game.undoLastTurn();

            beta = Math.min(beta, eval);

            if (beta <= alpha) {
                transpositionTable.put(hash, new HashEntry(beta, depth, HashEntryType.EXACT));
                return beta; // Prune remaining branches
            }
        }
        transpositionTable.put(hash, new HashEntry(beta, depth, HashEntryType.EXACT));
        return beta;
    }

    private double alphaBetaMax(Game game, double alpha, double beta, int depth) {
        long hash = Zobrist.hashify(game);
        HashEntry entry = transpositionTable.get(hash);
        if (entry != null && entry.getDepth() >= depth) {
            maxHits++;
            return entry.getScore();
        }

        if (depth == 0 || game.isFinished()) {
            double eval = Arrays.stream(heuristics).mapToDouble(c -> c.eval(game) * c.getWeight()).sum();
            transpositionTable.put(hash, new HashEntry(eval, depth, HashEntryType.EXACT));
            return eval;
        }

        var placements = getPossiblePlacements(game);

        for (var placement : placements) {
            game.takeTurn(placement);
            double eval = alphaBetaMin(game, alpha, beta, depth - 1);
            game.undoLastTurn();

            alpha = Math.max(alpha, eval);

            if (beta <= alpha) {
                // Store the result in the transposition table
                transpositionTable.put(hash, new HashEntry(alpha, depth, HashEntryType.EXACT));
                return beta; // Prune remaining branches
            }
        }

        transpositionTable.put(hash, new HashEntry(alpha, depth, HashEntryType.EXACT));
        return alpha;
    }

    private Placement alphaBetaSearch(Game game, double alpha, double beta, int depth) {
        var placements = getPossiblePlacements(game);

        Placement best = null;
        for (var placement : placements) {
            game.takeTurn(placement);
            double eval = alphaBetaMin(game, alpha, beta, depth - 1);
            game.undoLastTurn();

            if (eval >= alpha) {
                alpha = eval;
                best = placement;
            }
        }

        System.out.println("Done with alpha beta search");
        return best;
    }

    private double negamax(final Game game, double alpha, double beta, int depth){
        long hash = Zobrist.hashify(game);
        HashEntry entry = transpositionTable.get(hash);
        if (entry != null && entry.getDepth() >= depth) {
            maxHits++;
            return entry.getScore();
        }

        if(depth == 0 || game.isFinished()){
            return Arrays.stream(heuristics).mapToDouble(c -> c.eval(game) * c.getWeight()).sum();
        }

        double score;
        var placements = getPossiblePlacements(game);
        for(var placement : placements){
            game.takeTurn(placement, false);
            score = -negamax(game, -beta, -alpha, depth -1);
            if(score > alpha){
                alpha = score;
                if(alpha >= beta){
                    break;
                }
            }
        }

        return alpha;
    }

    private Placement negamaxPlacement(final Game game, int depth){
        var placements = getPossiblePlacements(game);
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        double score = Double.NEGATIVE_INFINITY;
        Placement best = null;
        for(var placement : placements){
            game.takeTurn(placement);
            double eval = negamax(game, -beta, -alpha, depth - 1);
            if(eval >= score){
                score = eval;
                best = placement;
            }
            game.undoLastTurn();
        }

        return best;
    }

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        int depth = 2;
        if(prefetch != null){
            try {
                prefetch.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        Placement best = alphaBetaSearch(game, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, depth); //negamaxPlacement(game, depth);
        if (DEBUG) {
            game.takeTurn(best);
            for (var heuristic : this.heuristics) {
                double s = heuristic.eval(game) * heuristic.getWeight();
                System.out.println(heuristic.getClass().getName() + " : " + s);
            }
            game.undoLastTurn();
        }

        var cp = game.copy();
        cp.takeTurn(best, false);
        prefetch = executorService.submit(() -> negamax(cp, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1));


        System.out.println("Max-Hits: " + maxHits + " Min-Hits: " + minHits);
        maxHits = 0;
        minHits = 0;
        return Optional.ofNullable(best);
    }
}
