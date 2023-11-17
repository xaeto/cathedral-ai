package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum StageBuildings {
    WhiteEarlyGameBuildings(new Building[]{
            Building.Blue_Cathedral
    }),

    WhiteMidGameBuildings(new Building[]{
            Building.White_Academy,
            Building.White_Stable
    }),

    WhiteEndgameGameBuildings(new Building[]{
    }),

    BlackEarlyGameBuildings(new Building[]{
            Building.Black_Academy,
            Building.Black_Stable
    }),

    BlackMidGameBuildings(new Building[]{
            Building.Black_Academy,
            Building.Black_Stable
    }),

    BlackEndgameGameBuildings(new Building[]{
    }),

    None(new Building[]{});


    private Building[] preferedBuildings;
    StageBuildings(Building[] preferedBuildings){
        this.preferedBuildings = preferedBuildings;
    }

    public static StageBuildings getStage(Game game, Color player){
        int stage = game.lastTurn().getTurnNumber();
        System.out.println("Current Stage: " + game.lastTurn().getTurnNumber());

        StageBuildings buildings;
        if(player == Color.White || player == Color.Blue){
            buildings = getWhiteBuildings(stage);
        } else {
            buildings = getBlackBuildings(stage);
        }

        return buildings;
    }

    private static StageBuildings getBlackBuildings(int stage){
        if(stage >= 0 && stage <= 3){
            return BlackEarlyGameBuildings;
        }
        if(stage >= 5 && stage <= 8){
            return BlackMidGameBuildings;
        }

        return BlackEndgameGameBuildings;
    }

    private static StageBuildings getWhiteBuildings(int stage){
        if(stage >= 0 && stage <= 3){
            return WhiteEarlyGameBuildings;
        }
        if(stage >= 5 && stage <= 8){
            return WhiteMidGameBuildings;
        }

        return WhiteEndgameGameBuildings;
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