package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;

public class CathedralHeuristic extends Heuristic {
    public CathedralHeuristic(double weight) {
        super(weight);
    }

    @Override
    public double eval(Game game) {
        boolean hasCathedral = game.getBoard().getPlacedBuildings().stream().anyMatch(c-> c.building() == Building.Blue_Cathedral);
        if(game.getCurrentPlayer() == Color.Black){
            return hasCathedral ? 0 : 1;
        } else {
            return hasCathedral ? 1 : -1;
        }
    }
}
