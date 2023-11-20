package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;

import java.util.*;

public class SmartAgent implements Agent {
    private List<Placement> getPlaceableBuildings(Game game){
        var placeables = new ArrayList<Placement>();
        var buildings = game.getPlacableBuildings(game.getCurrentPlayer());
        for(var building : buildings){
            for(var direction : building.getTurnable().getPossibleDirections()){
                for(int y = 0; y < 10; ++y){
                    for(int x = 0; x < 10; ++x){
                        var p = new Placement(y, x, direction, building);
                        if(game.takeTurn(p)){
                            game.undoLastTurn();
                            placeables.add(p);
                        }
                    }
                }
            }
        }

        return placeables;
    }

    private int countPlacedBuildings(Board board, Color player) {
        return (int) board.getPlacedBuildings().stream()
                .filter(placement -> placement.building().getColor() == player)
                .count();
    }

    private Color getOpponentPlayer(Color currentPlayer) {
        return (currentPlayer == Color.Black) ? Color.White : Color.Black;
    }

    public Placement findBestPlacement(Game game, int depth) {
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        List<Placement> possiblePlacements = getPlaceableBuildings(game);
        if (possiblePlacements.size() == 1) {
            return possiblePlacements.get(0);
        }

        int score = Integer.MIN_VALUE;
        Placement bestPlacement = null;

        for (Placement placement : possiblePlacements) {
            Game child = game.copy();
            child.takeTurn(placement);
            int childScore = alphaBetaSearch(child, depth - 1, alpha, beta, true);

            if (childScore > score) {
                score = childScore;
                bestPlacement = placement;
            }

            alpha = Math.max(alpha, childScore);
            if (alpha >= beta) {
                break;
            }
        }

        return bestPlacement;
    }

    private int heuristik(Game game){
        int playerScore = countPlacedBuildings(game.getBoard(), game.getCurrentPlayer());
        int opponentScore = countPlacedBuildings(game.getBoard(), getOpponentPlayer(game.getCurrentPlayer()));

        return playerScore - opponentScore;
    }

    private int alphaBetaSearch(Game node, int depth, int alpha, int beta, boolean maximizing) {
        if (depth == 0 || node.isFinished()) {
            return heuristik(node);
        }

        List<Placement> possiblePlacements = getPlaceableBuildings(node);
        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Placement placement : possiblePlacements) {
                Game child = node.copy();
                child.takeTurn(placement);
                int eval = alphaBetaSearch(child, depth - 1, alpha, beta, true);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Placement placement : possiblePlacements) {
                Game child = node.copy();
                child.takeTurn(placement);
                int eval = alphaBetaSearch(child, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }
    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        var placement = findBestPlacement(game, 1);
        return Optional.of(placement);
    }
}
