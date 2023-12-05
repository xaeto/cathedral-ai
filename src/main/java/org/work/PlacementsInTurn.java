package org.work;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Building;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Placement;
import java.util.concurrent.atomic.AtomicInteger;

// Arbeit um alle möglichen Placements in einem Board herauszufinden und in Auftrag zu geben
public class PlacementsInTurn implements Work{
  public static AtomicInteger calls = new AtomicInteger(0);

  private final Board board;
  private final Color player;

  public PlacementsInTurn(Board board, Color player) {
    synchronized (calls) {
      calls.addAndGet(1);
    }

    this.board = board;
    this.player = player;
  }

  @Override
  public void work() {
    if (workToDo.size() > 500000){
      workToDo.add(this);
    } else {
      for (Building building : board.getPlacableBuildings(player)) {
        for (Placement placement : building.getAllPossiblePlacements()) {
          Work.workToDo.add(new PlacementCalculation(placement, board.copy()));
        }
      }
      synchronized (calls) {
        calls.addAndGet(-1);
      }
    }
  }
}
