package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.*;
import org.example.NeuralNetwork;

import java.util.*;

public class SaintAgent implements Agent {
    @Override
    public String name(){
        return "Saint";
    }
    private List<Placement> generateRandomPlacements(Game game) {
        List<Placement> possibleTurns = new ArrayList<Placement>();
        for(Building building : game.getPlacableBuildings()){
            for(Direction direction : building.getTurnable().getPossibleDirections()){
                for(int x = 0; x < 10; ++x){
                    for(int y = 0; y < 10; ++y){
                        var placement = new Placement(new Position(x,y), direction, building);
                        if(game.takeTurn(placement)){
                            possibleTurns.add(placement);
                            game.undoLastTurn();
                        }
                    }
                }
            }
        }
        return possibleTurns;
    }
    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        var possibleTurns = generateRandomPlacements(game);
        try{
            if(!possibleTurns.isEmpty()){
                Placement max = Collections.max(possibleTurns, Comparator.comparing(c -> c.building().score()));
                return Optional.of(max);
            }
        } finally {
        }
        return Optional.empty();
    }
}
