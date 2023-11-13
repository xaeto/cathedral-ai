package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.example.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlphaBetaAgent implements Agent {
    private static final int MAX_DEPTH = 1;

    public AlphaBetaAgent() {
    }

    private List<Placement> getPossiblePlacements(Game game){
        var buildings = game.getPlacableBuildings(game.getCurrentPlayer());
        var placements = new ArrayList<Placement>();

        for(var building: buildings){
            placements.addAll(building.getPossiblePlacements(game));
        }
        return placements;
    }

    private List<Placement> getPossiblePlacements(Board board, Color color){
        var buildings = board.getPlacableBuildings(color);
        var placements = new ArrayList<Placement>();

        for(var building: buildings){
            var possiblePlacements = building.getPossiblePlacements(board);
            placements.addAll(possiblePlacements);
        }
        return placements;
    }

    private double miniMaxParallel(Game state, int depth, double alpha, double beta, boolean maximizePlayer) {
        if (depth == 0) {
            return Heuristic.calculateHeuristics(state);
        }

        List<Placement> turns = getPossiblePlacements(state.getBoard(), state.getCurrentPlayer());

        return turns.parallelStream().mapToDouble(turn -> {
            var gameCopy = state.copy();
            gameCopy.takeTurn(turn, false);
            return miniMax(gameCopy, depth - 1, alpha, beta, !maximizePlayer);
        }).max().orElse(maximizePlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
    }

    private double miniMax(Game game, int depth, double alpha, double beta, boolean maximizePlayer){
        if(depth == 0){
            return Heuristic.calculateHeuristics(game);
        }

        var state = game.copy();
        if(maximizePlayer){
            double max = Double.NEGATIVE_INFINITY;
            var turns = getPossiblePlacements(state.getBoard(), state.getCurrentPlayer());
            for(var turn : turns){
                state.takeTurn(turn);
                double eval = miniMax(state, depth -1, alpha, beta, false);
                state.undoLastTurn();
                max = Math.max(max, eval);
                alpha = Math.max(alpha, eval);
                if(beta <= alpha){
                    break;
                }
            }
            return max;
        } else {
            double min = Double.POSITIVE_INFINITY;
            var turns = getPossiblePlacements(state.getBoard(), state.getCurrentPlayer());
            for (var turn : turns){
                state.takeTurn(turn);
                double eval = miniMax(state, depth -1, alpha, beta, true);
                state.undoLastTurn();
                min = Math.min(min, eval);
                beta = Math.min(beta, eval);
                if(beta <= alpha){
                    break;
                }
            }
            return min;
        }
    }

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        var turns = getPossiblePlacements(game);
        Placement placement = null;
        double max = Double.NEGATIVE_INFINITY;

        for(var turn : turns){
            game.takeTurn(turn);
            double eval = miniMaxParallel(game, MAX_DEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
            game.undoLastTurn();
            if (eval >= max) {
                placement = turn;
                max = eval;
            }
        }

        System.out.println("Score: " + max + " " + placement);
        return Optional.of(placement);
    }
}
