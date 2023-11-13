package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class McState implements Serializable {
    private Board board;
    private Color playerColor;
    private int visitCount = 0;
    private double score = 0;

    public McState(Board board, Color playerColor){
        this.board = board;
        this.playerColor = playerColor;
    }

    public void increaseVisitCount(){
        this.visitCount++;
    }

    public double getScore(){
        return this.score;
    }

    public void setScore(double score){
        this.score = score;
    }

    public List<McState> getPossibleStates(){
        var buildings = this.board.getPlacableBuildings(this.playerColor);
        var states = new ArrayList<McState>();
        for(var building : buildings){
            for(var placement : building.getPossiblePlacements(board)){
                var board = this.board.copy();
                board.placeBuilding(placement, true);
                var game = new Game(board);
                double score = Heuristic.calculateZoneHeuristic(game);
                var state = new McState(board, this.playerColor.opponent());
                state.setScore(score);
                states.add(state);
            }
        }

        return states;
    }
}
