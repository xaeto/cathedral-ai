package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedrale.heuristics.Heuristic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CuteAgent implements Agent {
    private HeuristicInterface eval = game -> Heuristic.calculateZoneHeuristic(game);
    private List<Placement> getPossiblePlacements(Game game) {
        var buildings = game.getPlacableBuildings(game.getCurrentPlayer());
        var placements = new ArrayList<Placement>();

        for (var building : buildings) {
            placements.addAll(building.getPossiblePlacements(game));
        }
        return placements;
    }

    private boolean placementPlaced(List<Placement> placedBuildings, Placement placement){
        return placedBuildings.stream().anyMatch(c-> c.building() == placement.building()
                && c.x() == placement.x()
                && c.y() == placement.y()
                && c.direction() == placement.direction());
    }

    private Optional<Placement> tryCombinations(Game game){
        BuildingCombo[] combos;
        if(game.getCurrentPlayer() == Color.Black){
            combos = new BuildingCombo[]{BuildingCombo.BLACK_COMBOS};
        } else {
            combos = new BuildingCombo[]{BuildingCombo.WHITE_COMBOS};
        }

        // test initializers
        double max = 0;
        Placement initializer = null;
        for(var combo : combos){
            for(var variants : combo.getVariants()){
                Placement init = variants.getPlacements()[0];
                if(game.takeTurn(init, false)){
                    double score = eval.eval(game);
                    if(score >= max){
                        initializer = init;
                        max = score;
                    }
                    game.undoLastTurn();
                }
            }
        }

        if(initializer != null){
            return Optional.of(initializer);
        }

        Placement placement = null;
        var placedBuildings = game.getBoard().getPlacedBuildings();
        for(var combo : combos){
            var variants = combo.getVariants();
            for(var variant : variants){
                var placements = variant.getPlacements();
                Placement init = placements[0];
                Placement turn = placements[1];
                if(placementPlaced(placedBuildings, init)){
                    if(game.takeTurn(turn, false)){
                        placement = turn;
                        game.undoLastTurn();
                    }
                }
            }
        }

        if(placement == null){
            return Optional.empty();
        }

        return Optional.of(placement);
    }

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        var stage = StageBuildings.getStage(game, game.getCurrentPlayer());

        var combination = tryCombinations(game);
        if(!combination.isEmpty()){
            return combination;
        }

        WeightedPlacement best = null;
        if(stage == StageBuildings.WhiteEarlyGameBuildings || stage == StageBuildings.BlackEarlyGameBuildings){
            double max = Double.NEGATIVE_INFINITY;
            for(var placement : stage.getWeightedPlaceableBuildings(game, eval)){
                if(placement.getWeight() >= max){
                    max = placement.getWeight();
                    best = placement;
                }
            }
        } else if(stage == StageBuildings.BlackMidGameBuildings || stage == StageBuildings.WhiteMidGameBuildings) {
            var bestPlacement = stage.getBestPlacement(game, 1);
            if(!bestPlacement.isEmpty()){
                best = bestPlacement.get();
            }
        } else if(stage == StageBuildings.BlackEndgameGameBuildings || stage == StageBuildings.WhiteEndgameGameBuildings){
            var bestPlacement = stage.getBestPlacement(game, 1);
            if(!bestPlacement.isEmpty()){
                best = bestPlacement.get();
            }
        }

        if(best == null){
            var placements = getPossiblePlacements(game);
            if(placements.isEmpty()){
                return Optional.empty();
            }
            Placement rnd = placements.get(new Random().nextInt(0, placements.size() + 1));
            return Optional.of(rnd);
        }

        System.out.println("Score: " + best.getWeight() + " " + best);

        return Optional.of(best);
    }
}
