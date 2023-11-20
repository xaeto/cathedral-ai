package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import io.aeron.shadow.org.HdrHistogram.DoubleLinearIterator;
import org.board.fast.FastBoard;
import org.cathedrale.heuristics.Heuristic;
import org.encog.util.Stopwatch;

import java.util.*;

public class HeuristicalAgent implements Agent {
    private Heuristic[] heuristics;
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

    private double estimate(Game game, int depth){
        var cp = game.copy();
        cp.forfeitTurn();
        if(depth == 0){
            return Arrays.stream(heuristics).mapToDouble(c-> c.eval(cp) * c.getWeight()).sum();
        }

        Placement best = null;
        double max = Double.NEGATIVE_INFINITY;
        var turns = getPossiblePlacements(game);
        for(var turn : turns){
            cp.forfeitTurn();
            cp.takeTurn(turn, false);
            double score = Arrays.stream(heuristics).mapToDouble(c-> c.eval(game) * c.getWeight()).sum();
            if(score >= max){
                max = score;
                best = turn;
            }
            cp.forfeitTurn();
        }

        cp.takeTurn(best, false);
        return estimate(cp, depth - 1);
    }

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        double score = Double.NEGATIVE_INFINITY;
        Placement best = null;

        var placements = getPossiblePlacements(game);
        for(var placement : placements){
            game.takeTurn(placement, true);
            double eval = Arrays.stream(heuristics).mapToDouble(c-> c.eval(game) * c.getWeight()).sum();
            if(eval >= score){
                score = eval;
                best = placement;
            }
            game.undoLastTurn();
        }
        return Optional.of(best);
    }
}
