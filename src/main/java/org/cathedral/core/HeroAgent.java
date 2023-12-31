package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.*;
import org.example.Network;
import org.example.NeuralNetwork;
import org.opencv.dnn.Net;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class HeroAgent implements Agent {
    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {

        List<Placement> possibleTurns = new ArrayList<Placement>();
        for(Building building : game.getPlacableBuildings()){
            for(Direction direction : building.getTurnable().getPossibleDirections()){
                for(int x = 0; x < 10; ++x){
                    for(int y = 0; y < 10; ++y){
                        var placement = new Placement(new Position(x,y), direction, building);
                        if(game.takeTurn(placement, true)){
                            possibleTurns.add(placement);
                            game.undoLastTurn();
                        }
                    }
                }
            }
        }

        if(!possibleTurns.isEmpty()){
            var turn = possibleTurns.get(new Random().nextInt(possibleTurns.size()));
            Network.printMatrix(game.getBoard().getField());
            System.out.println(Network.getFieldCountByColor(game.getBoard(), game.getCurrentPlayer().subColor()));
            System.out.println(Network.getFieldCountByColor(game.getBoard(), game.getCurrentPlayer().opponent().subColor()));
            System.out.println();
            return Optional.of(turn);
        }

        return Optional.empty();
    }
}
