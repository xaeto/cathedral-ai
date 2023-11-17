package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedrale.heuristics.Heuristic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

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

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        var buildings = game.getPlacableBuildings(game.getCurrentPlayer());
        var placements = new ArrayList<Placement>();
        for(var building : buildings){
            placements.addAll(building.getPossiblePlacements(game.getBoard()));
        }

        double score = Double.NEGATIVE_INFINITY;
        Placement best = null;
        for(var placement : placements){
            game.takeTurn(placement, false);
            double nextScore = Arrays.stream(heuristics).mapToDouble(c-> c.eval(game) * c.getWeight()).sum();
            if(nextScore >= score){
                best = placement;
                score = nextScore;
            }
            game.undoLastTurn();
        }
        return Optional.of(best);
    }
}
