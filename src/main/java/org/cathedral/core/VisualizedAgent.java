package org.cathedral.core;

import de.fhkiel.ki.cathedral.ai.Agent;
import de.fhkiel.ki.cathedral.game.Game;
import de.fhkiel.ki.cathedral.game.Placement;
import org.cathedral.core.evaluators.Evaluator;

import java.util.Optional;

public class VisualizedAgent implements Agent {
    private Evaluator evaluator;

    @Override
    public String name() {
        return this.evaluator.getClass().getName();
    }

    public VisualizedAgent(Evaluator evaluator){
        this.evaluator = evaluator;
    }

    @Override
    public Optional<Placement> calculateTurn(Game game, int i, int i1) {
        Placement best = evaluator.eval(game);
        return Optional.of(best);
    }
}
