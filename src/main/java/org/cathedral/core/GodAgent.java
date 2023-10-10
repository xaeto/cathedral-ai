package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.*;
import org.example.NeuralNetwork;

import java.util.Optional;

public class GodAgent implements Agent {
    @Override
    public String name() {
        return "God";
    }
    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        return Optional.empty();
    }
}
