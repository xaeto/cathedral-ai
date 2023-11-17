package org.cathedrale.heuristics;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;

public class HeuristicsHelper {
    public static int countFieldById(Board board, Color color){
        var field = board.getField();
        int count = 0;
        for(int y = 0; y < 10; ++y){
            for(int x = 0; x < 10; ++x){
                if(field[y][x] == color){
                    count++;
                }
            }
        }

        return count;
    }
}
