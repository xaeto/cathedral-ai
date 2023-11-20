package org.cathedral.core;
import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.*;
import java.util.*;
import java.util.stream.Collectors;

public enum GameStage {
    OPENER,
    EARLYGAME,
    MIDGAME,
    LATEGAME;

    public static GameStage determineStage(Game game) {

        int movesPlayed = game.getBoard().getPlacedBuildings().size();

        if(movesPlayed < 3){
            return OPENER;
        } else if (movesPlayed < 8) {
            return EARLYGAME;
        } else if (movesPlayed < 9) {
            return MIDGAME;
        } else {
            return LATEGAME;
        }
    }


}
