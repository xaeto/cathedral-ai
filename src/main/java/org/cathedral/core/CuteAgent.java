package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;

import java.util.Optional;

public class CuteAgent implements Agent {
    private HeuristicInterface eval = game -> Heuristic.calculateZoneHeuristic(game);

    private Optional<Placement> tryCombinations(Game game){
        BuildingCombo[] combos;
        if(game.getCurrentPlayer() == Color.Black){
            combos = new BuildingCombo[]{BuildingCombo.BLACK_ACADEMY_STABLE};
        } else {
            combos = new BuildingCombo[]{BuildingCombo.WHITE_ACADEMY_STABLE};
        }

        Placement placement = null;
        var placedBuildings = game.getBoard().getPlacedBuildings();
        for(var combo : combos){
            var variants = combo.getVariants();
            for(var variant : variants){
                var placements = variant.getPlacements();
                Placement init = placements[0];
                Placement turn = placements[1];
                if(placedBuildings.stream().anyMatch(c-> c.building() == init.building()
                        && c.x() == init.x()
                        && c.y() == init.y()
                        && c.direction() == init.direction())){

                    if(game.takeTurn(turn, false)){
                        placement = turn;
                    }
                    game.undoLastTurn();
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
            System.out.println("Found possible combination");
            return combination;
        }

        WeightedPlacement best = null;
        double max = 0;
        for(var placement : stage.getWeightedPlaceableBuildings(game, eval)){
            if(placement.getWeight() >= max){
                max = placement.getWeight();
                best = placement;
            }
        }

        System.out.println("Score: " + best.getWeight() + " " + best);

        return Optional.of(best);
    }
}
