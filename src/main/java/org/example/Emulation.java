package org.example;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;
import freemarker.cache._CacheAPI;

public class Emulation {
    private int _count;
    private Board[] _boards;

    public Emulation(int count){
        _count = count;
        _boards = new Board[_count];
    }

    private void emulate(Board board, Color player){
        var buildings = board.getPlacableBuildings(player);
    }

    public void start(){
        Color player = Color.White;
        for(var board : _boards){
            emulate(board, player);
        }
    }
}
