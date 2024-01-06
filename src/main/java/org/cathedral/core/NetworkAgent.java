package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.example.NeuralNetwork;

import java.util.Optional;

public class NetworkAgent implements Agent {
    public NetworkAgent(NeuralNetwork neuralNetwork){

    }

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        return Optional.empty();
    }
}
