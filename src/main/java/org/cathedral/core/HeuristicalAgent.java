package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import io.aeron.shadow.org.HdrHistogram.DoubleLinearIterator;
import org.board.fast.FastBoard;
import org.cathedrale.heuristics.Heuristic;
import org.encog.util.Stopwatch;
import org.work.Zobrist;

import java.util.*;
import java.util.concurrent.*;

public class HeuristicalAgent implements Agent {
    private Heuristic[] heuristics;
    private HashMap<Long, HashEntry> transpositionTable = new HashMap<>();

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

    private List<Placement> getPossiblePlacements(Game game){
        var buildings = game.getPlacableBuildings(game.getCurrentPlayer());
        var placements = new ArrayList<Placement>();
        for(var building : buildings){
            placements.addAll(building.getPossiblePlacements(game.getBoard()));
        }

        return placements;
    }


    private double negamax(final Game game, double alpha, double beta, int depth, Stopwatch watch){
        if (depth == 0 || game.isFinished() || watch.getElapsedMilliseconds() >= 12000) {
            long hash = Zobrist.hashify(game.getBoard());
            if(transpositionTable.containsKey(hash)){
                HashEntry entry = transpositionTable.get(hash);
                return entry.getScore();
            }
            double score = Arrays.stream(heuristics).mapToDouble(c-> c.eval(game) * c.getWeight()).sum();
            var entry = new HashEntry(score, depth, HashEntryType.EXACT);
            transpositionTable.put(hash, entry);
            return score;
        }

        var placements = getPossiblePlacements(game);
        double score = Double.NEGATIVE_INFINITY;
        for(var placement : placements){
            game.takeTurn(placement, false);
            score = -negamax(game, -alpha, -beta, depth -1, watch);
            game.undoLastTurn();
            if(score >= beta) return beta;
            if(score > alpha) alpha = score;
        }

        return alpha;
    }

    private double maxValue(Game game, double alpha, double beta, int depth, Stopwatch watch) {
        if (depth == 0 || game.isFinished() || watch.getElapsedMilliseconds() >= 12000) {
            long hash = Zobrist.hashify(game.getBoard());
            if(transpositionTable.containsKey(hash)){
                HashEntry entry = transpositionTable.get(hash);
                return entry.getScore();
            }
            double score = Arrays.stream(heuristics).mapToDouble(c-> c.eval(game) * c.getWeight()).sum();
            var entry = new HashEntry(score, depth, HashEntryType.EXACT);
            transpositionTable.put(hash, entry);
            return score;
        }

        var stage = StageBuildings.getStage(game, game.getCurrentPlayer());
        double max = Double.NEGATIVE_INFINITY;
        var placements = stage.getPlaceableBuildings(game);
        for (Placement placement : placements) {
            game.takeTurn(placement, false);
            max = Math.max(max, minValue(game, alpha, beta, depth - 1, watch));
            game.undoLastTurn();
            if (max >= beta) {
                return max;
            }
            alpha = Math.max(alpha, max);
        }
        return max;
    }

    private double minValue(Game game, double alpha, double beta, int depth, Stopwatch watch) {
        if (depth == 0 || game.isFinished() || watch.getElapsedMilliseconds() >= 12000) {
            long hash = Zobrist.hashify(game.getBoard());
            if(transpositionTable.containsKey(hash)){
                HashEntry entry = transpositionTable.get(hash);
                return entry.getScore();
            }
            double score = Arrays.stream(heuristics).mapToDouble(c-> c.eval(game) * c.getWeight()).sum();
            var entry = new HashEntry(score, depth, HashEntryType.EXACT);
            transpositionTable.put(hash, entry);
            return score;
        }
        double min = Double.POSITIVE_INFINITY;
        var stage = StageBuildings.getStage(game, game.getCurrentPlayer());
        var placements = stage.getPlaceableBuildings(game);

        for (Placement placement : placements.subList(0, placements.size()/(game.lastTurn().getTurnNumber() + 1))) {
            game.takeTurn(placement, false);
            min = Math.min(min, maxValue(game, alpha, beta, depth - 1, watch));
            game.undoLastTurn();
            if (min <= alpha) {
                return min;
            }
            beta = Math.min(beta, min);
        }
        return min;
    }

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        var stage = StageBuildings.getStage(game, game.getCurrentPlayer());
        var placements = stage.getPlaceableBuildings(game);
        double score = -1000;
        Placement best = null;

        var watch = new Stopwatch();
        watch.start();
        for(int depth = 1; depth < 10 || watch.getElapsedMilliseconds() >= 12000; ++depth){
            if(watch.getElapsedMilliseconds() >= 12000)
                break;
            System.out.println("Current Depth: " + (depth));
            for (var placement : placements) {
                if(watch.getElapsedMilliseconds() >= 12000)
                    break;
                game.takeTurn(placement, false);
                double eval = maxValue(game, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, depth, watch);
                if(eval >= score){
                    best = placement;
                }
                game.undoLastTurn();
            }
        }
        watch.stop();

        return Optional.of(best);
    }
}
