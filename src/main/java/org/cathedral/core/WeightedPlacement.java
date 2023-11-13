package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.*;

public class WeightedPlacement extends Placement {
    private double weight;
    private Color placedBy = Color.None;

    public WeightedPlacement(Position position, Direction direction, Building building) {
        super(position, direction, building);
    }

    public WeightedPlacement(int x, int y, Direction direction, Building building) {
        super(x, y, direction, building);
    }

    public double getWeight(){
        return this.weight;
    }

    public void setWeight(double weight){
        this.weight = weight;
    }

    public void setPlacedBy(Color color){
        this.placedBy = color;
    }
}
