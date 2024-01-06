package org.cathedral.heuristics;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HeuristicsHelper {
    public static int countFieldById(Board board, Color color){
        var field = board.getField();
        int count = 0;
        for(int y = 0; y < 10; ++y){
            for(int x = 0; x < 10; ++x){
                if(field[y][x].getId() == color.getId()){
                    count++;
                }
            }
        }

        return count;
    }

    public static List<Placement> getPossiblePlacements(final Game game){
        var buildings = game.getBoard().getPlacableBuildings(game.getCurrentPlayer());
        var placements = new ArrayList<Placement>();
        for(var building : buildings){
            placements.addAll(building.getPossiblePlacements(game.getBoard()));
        }

        return placements;
    }
}
