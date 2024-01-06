package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;

import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedral.heuristics.Heuristic;

import java.util.*;

public enum StageBuildings {
    CathedralBuilding(Building.Blue_Cathedral),
    WhiteEarlyGameBuildings(Building.White_Academy, Building.White_Tower,Building.White_Infirmary,Building.White_Abbey,Building.White_Square, Building.White_Manor),
    WhiteMidGameBuildings(Building.White_Abbey, Building.White_Square, Building.White_Bridge, Building.White_Inn),
    WhiteEndgameGameBuildings(Building.White_Stable,Building.White_Tavern),
    BlackEarlyGameBuildings(Building.Black_Academy, Building.Black_Tower,Building.Black_Infirmary,Building.Black_Abbey,Building.Black_Square, Building.Black_Manor),
    BlackMidGameBuildings(Building.Black_Abbey, Building.Black_Square, Building.Black_Bridge, Building.Black_Inn),
    BlackEndgameGameBuildings(Building.Black_Stable,Building.Black_Tavern),

    None();
    private final Building[] preferedBuildings;

    StageBuildings(Building... preferedBuildings){
        this.preferedBuildings = preferedBuildings;
    }

    public static StageBuildings getStage(Game game, Color player){
        int stage = game.lastTurn().getTurnNumber();

        StageBuildings buildings;
        if(player == Color.White || player == Color.Blue){
            buildings = getWhiteBuildings(stage);
        } else {
            buildings = getBlackBuildings(stage);
        }

        return buildings;
    }

    private static StageBuildings getBlackBuildings(int stage){
        if(stage > 1 && stage <= 3){
            return BlackEarlyGameBuildings;
        }
        if(stage >= 5 && stage < 10){
            return BlackMidGameBuildings;
        }

        if(stage >= 10){
            return BlackEndgameGameBuildings;
        }

        return None;
    }

    private static StageBuildings getWhiteBuildings(int stage){
        if(stage == 0){
            return CathedralBuilding;
        }
        if(stage > 0 && stage < 5){
            return WhiteEarlyGameBuildings;
        }
        if(stage >= 5 && stage < 10){
            return WhiteMidGameBuildings;
        }

        if(stage >= 10){
            return WhiteEndgameGameBuildings;
        }

        return None;
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
                max = Math.max(max, miniMaxParallel(game, depth - 1, alpha, beta, false));
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
                min = Math.min(min, miniMaxParallel(game, depth - 1, alpha, beta, true));
                game.undoLastTurn();
                beta = Math.min(beta, min);
                if (beta <= alpha) {
                    break;
                }
            }
            return min;
        }
    }

    private double miniMaxParallel(Game state, int depth, double alpha, double beta, boolean maximizePlayer) {
        if (depth == 0) {
            return Heuristic.calculateZoneHeuristic(state);
        }

        List<Placement> turns = getPlaceableBuildings(state);
        return turns.parallelStream().mapToDouble(turn -> {
            var gameCopy = state.copy();
            gameCopy.takeTurn(turn, true);
            return miniMax(gameCopy, depth - 1, alpha, beta, !maximizePlayer);
        }).max().orElse(maximizePlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
    }

    public Optional<WeightedPlacement> getBestPlacement(Game game, int depth) {
        System.out.println("Depth: " + depth);
        var stage = getStage(game, game.getCurrentPlayer());
        var possiblePlacements = stage.getPlaceableBuildings(game);

        Optional<WeightedPlacement> placement = Optional.empty();
        double max = Double.NEGATIVE_INFINITY;
        for (var possiblePlacement : possiblePlacements) {
            game.takeTurn(possiblePlacement, false);
            double score = miniMax(game, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
            if (score >= max) {
                var weighted = new WeightedPlacement(possiblePlacement);
                weighted.setWeight(score);
                placement = Optional.of(weighted);
                max = score;
            }
            game.undoLastTurn();
        }

        return placement;
    }

    public List<WeightedPlacement> getWeightedPlaceableBuildings(Game game, HeuristicInterface heuristicInterface){
        var placements = getPlaceableBuildings(game);
        var weightedPlacements = new ArrayList<WeightedPlacement>();

        for(var placement : placements){
            game.takeTurn(placement, false);
            var weightedPlacement = new WeightedPlacement(placement);

            double eval = heuristicInterface.eval(game);
            weightedPlacement.setWeight(eval);
            weightedPlacements.add(weightedPlacement);
            game.undoLastTurn();
        }

        return weightedPlacements;
    }

    public List<Placement> getPlaceableBuildings(Game game){
        var placements = new ArrayList<Placement>();
        for(var building : this.preferedBuildings){
            placements.addAll(building.getPossiblePlacements(game));
        }

        // if preferred buildings are not placeable, return available placements without prefered placements
        if(placements.isEmpty()){
            var availableBuildings = game.getPlacableBuildings(game.getCurrentPlayer());
            for(var building : availableBuildings){
                placements.addAll(building.getPossiblePlacements(game));
            }
        }
        return placements;
    }
}