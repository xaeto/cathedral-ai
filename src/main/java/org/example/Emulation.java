package org.example;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import freemarker.cache._CacheAPI;

import java.util.ArrayList;
import java.util.Random;

public class Emulation {
    private int _count;
    private Game[] _games;

    public Emulation(int count){
        _count = count;
        _games = new Game[_count];
    }

    private boolean emulate(final Game game, final Color player){
        if(game.isFinished()){
            return true;
        }

        final var buildings = game.getBoard().getPlacableBuildings(player);
        final var placements = new ArrayList<Placement>();

        for(var building : buildings){
            placements.addAll(building.getPossiblePlacements(game));
        }

        for(var rnd : placements){
            // take first placement
            if(game.takeTurn(rnd, false)){
                return emulate(game, player.opponent());
            }
        }

        return emulate(game, player.opponent());
    }

    public void start(){
        Color player = Color.White;
        int finished = 0;
        for(var game : _games){
            game = new Game();
            while(!emulate(game, player)){
            }
        }
        System.out.println("Ran a total of " + finished + " Games.");
    }
}
