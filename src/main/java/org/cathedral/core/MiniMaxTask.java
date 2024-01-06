package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Game;
import org.cathedral.heuristics.Heuristic;

import java.util.concurrent.RecursiveTask;

import static org.cathedral.core.StageBuildings.getStage;

class MiniMaxTask extends RecursiveTask<Double> {
    private final Game game;
    private final int depth;
    private final double alpha;
    private final double beta;
    private final boolean maximizePlayer;

    public MiniMaxTask(Game game, int depth, double alpha, double beta, boolean maximizePlayer) {
        this.game = game;
        this.depth = depth;
        this.alpha = alpha;
        this.beta = beta;
        this.maximizePlayer = maximizePlayer;
    }

    @Override
    protected Double compute() {
        return miniMax(game, depth, alpha, beta, maximizePlayer);
    }

    private double miniMax(Game game, int depth, double alpha, double beta, boolean maximizePlayer){
        if(depth == 0){
            return Heuristic.calculateZoneHeuristic(game);
        }

        var stage = getStage(game, game.getCurrentPlayer());
        var turns = stage.getPlaceableBuildings(game);

        if (maximizePlayer) {
            double max = Double.NEGATIVE_INFINITY;
            for (var turn : turns) {
                game.takeTurn(turn, false);
                max = Math.max(max, miniMax(game, depth - 1, alpha, beta, false));
                game.undoLastTurn();
                alpha = Math.max(alpha, max);
                if (beta <= alpha) {
                    break;
                }
            }
            return max;
        } else {
            double min = Double.POSITIVE_INFINITY;
            for (var turn : turns) {
                game.takeTurn(turn, false);
                min = Math.min(min, miniMax(game, depth - 1, alpha, beta, true));
                game.undoLastTurn();
                beta = Math.min(beta, min);
                if (beta <= alpha) {
                    break;
                }
            }
            return min;
        }
    }
}
