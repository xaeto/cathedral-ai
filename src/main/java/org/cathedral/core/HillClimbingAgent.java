package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;

import java.util.Optional;

public class HillClimbingAgent implements Agent {
    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        return Optional.empty();
    }
}
