package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.*;
import org.example.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class GodAgent implements Agent {
    @Override
    public String name() {
        return "God";
    }
    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        try{
            var best = Network.GetBest(game, game.getBoard());
            Network.Add(game, best.get(), game.getBoard());
            return best;
        } finally {
            Network.Save(this.name());
        }
    }
}
