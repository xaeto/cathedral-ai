package org.cathedral.core;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Placement;

public record BoardPlacementScore(Placement placement, Board board, double score) {
}
