package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.*;
import org.cathedrale.heuristics.Heuristic;

import java.util.ArrayList;

public enum BuildingCombo {
    BLACK_ACADEMY_STABLE(10,
            new BuildingVariant(
                    new Placement(new Position(5,8), Direction._0, Building.Black_Academy),
                    new Placement(new Position(8,6), Direction._270, Building.Black_Tower)
            )
    ),
    WHITE_ACADEMY_STABLE(10,
            new BuildingVariant(
                    new Placement(new Position(1,3), Direction._0, Building.White_Academy),
                    new Placement(new Position(4,1), Direction._0, Building.White_Tower)
            ),
            new BuildingVariant(
                    new Placement(new Position(8,2), Direction._0, Building.White_Academy),
                    new Placement(new Position(5,1), Direction._0, Building.White_Tower)
            )
    ),
    WHITE_COMBOS(10, calculateZoneCombination(Color.White)),
    BLACK_COMBOS(10, calculateZoneCombination(Color.Black));

    private BuildingVariant[] variants;
    private double score;

    BuildingCombo(double score, BuildingVariant... variants){
        this.variants = variants;
        this.score = score;
    }

    public BuildingVariant[] getVariants(){
        return this.variants;
    }

    private static BuildingVariant[] calculateZoneCombination(Color color){
        var variants = new ArrayList<BuildingVariant>();
        var game = new Game();
        if(color == Color.White){
            game.forfeitTurn();
        }
        var placeables = game.getPlacableBuildings(game.getCurrentPlayer());
        for(var building : placeables){
            for(var placement : building.getPossiblePlacements(game)){
                game.takeTurn(placement, false);
                game.forfeitTurn();
                var futurePlacements = game.getPlacableBuildings(game.getCurrentPlayer());
                for(var futureBuilding : futurePlacements){
                    if(futureBuilding == building)
                        continue;
                    for(var futurePlacement : futureBuilding.getPossiblePlacements(game)){
                        if(game.takeTurn(futurePlacement, false)){
                            double score = Heuristic.countFieldsByPlayerId(game.getBoard(),
                                    game.getCurrentPlayer().opponent().subColor()
                            );
                            if(score > 0){
                                variants.add(new BuildingVariant(placement, futurePlacement));
                            }
                            game.undoLastTurn();
                        }
                    }
                }
            }
            game.undoLastTurn();
        }

        return variants.toArray(new BuildingVariant[0]);
    }
}
