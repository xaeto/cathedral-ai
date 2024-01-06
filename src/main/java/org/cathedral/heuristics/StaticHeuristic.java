package org.cathedral.heuristics;

import de.fhkiel.ki.cathedral.game.Game;

public class StaticHeuristic {
    private static double calculateMaterial(Game game){
        return HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().opponent());
    }

    private static double calculateZone(Game game){
        return HeuristicsHelper.countFieldById(game.getBoard(), game.getCurrentPlayer().opponent().subColor());
    }

    public static double eval(Game game){
        return calculateMaterial(game) + calculateZone(game)*5;
    }
}
