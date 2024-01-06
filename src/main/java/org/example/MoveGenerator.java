package org.example;

import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedral.heuristics.ZoneHeuristic;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    public static boolean isZoneCaptured(Game game, int n) {
        for (int i = 0; i < n; i++) {
            Placement placement = generateRandomPlacement(game);
            game.takeTurn(placement);
            game.forfeitTurn();
        }

        ZoneHeuristic zoneHeuristic = new ZoneHeuristic(1);
        double zoneScore = zoneHeuristic.eval(game, 1);

        double threshold = 0.8;
        return zoneScore > threshold;
    }

    private static Placement generateRandomPlacement(Game game) {
        List<Building> placableBuildings = game.getPlacableBuildings();
        Building randomBuilding = placableBuildings.get((int) (Math.random() * placableBuildings.size()));

        List<Placement> possiblePlacements = randomBuilding.getPossiblePlacements(game.getBoard()).stream().toList();
        Placement randomPlacement = possiblePlacements.get((int) (Math.random() * possiblePlacements.size()));

        return randomPlacement;
    }

    public static List<List<Placement>> generateZonePlacements(){
        var placements = new ArrayList<List<Placement>>();

        for(int i = 0; i < 10000; ++i){
            var game = new Game();
            if(isZoneCaptured(game, 2)){
                placements.add(game.getBoard().getPlacedBuildings());
            }
        }
        return placements;
    }
}
