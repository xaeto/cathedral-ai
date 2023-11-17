package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Direction;
import de.fhkiel.ki.cathedral.game.Placement;
import de.fhkiel.ki.cathedral.game.Position;

public enum BuildingCombo {
    BLACK_ACADEMY_STABLE(10,
            new BuildingVariant(
                    new Placement(new Position(5,8), Direction._0, Building.Black_Academy),
                    new Placement(new Position(8,6), Direction._270, Building.Black_Tower)
            )
    ),
    WHITE_ACADEMY_STABLE(10,
        new BuildingVariant(
                new Placement(new Position(5,8), Direction._0, Building.White_Academy),
                new Placement(new Position(8,6), Direction._0, Building.White_Tower)
        )
    );

    private BuildingVariant[] variants;
    private double score;
    BuildingCombo(double score, BuildingVariant... variants){
        this.variants = variants;
        this.score = score;
    }

    public BuildingVariant[] getVariants(){
        return this.variants;
    }
}
