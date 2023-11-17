package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Placement;

import java.util.ArrayList;
import java.util.List;

public class BuildingVariant {
    private Placement[] placements;

    public BuildingVariant(Placement... placements){
        this.placements = placements;
    }

    public Placement[] getPlacements(){
        return this.placements;
    }
}
