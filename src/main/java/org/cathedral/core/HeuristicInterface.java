package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Game;

@FunctionalInterface
public interface HeuristicInterface {
    double eval(Game game);
}
