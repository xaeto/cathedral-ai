package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.example.NeuralNetwork;

import java.util.Optional;

public class SmartAgent implements Agent {
    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        // segment board into chunks, divide x / 2 and y / 2
        var board = NeuralNetwork.GenerateBoardMatrix(game);
        var placeables = game.getPlacableBuildings(game.getCurrentPlayer());

        var placedBuildings = game.getPlacableBuildings()
                .stream()
                .filter(c-> placeables.stream().anyMatch(q -> q.getId() == c.getId()));

        for(int x = 0; x < 10; x++){
            for(int y = 0; y < 10; y++){
                // skip empty
                if(board[x][y] > 0){

                }
            }
        }
        return Optional.empty();
    }
}
