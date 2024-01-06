package org.work;

import de.fhkiel.ki.cathedral.game.Board;
import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Placement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

// Arbeit um ein Placement zu setzen
public  class PlacementCalculation implements Work{
  public static AtomicInteger calls = new AtomicInteger(0);
  public final Placement placement;
  public final Board board;

  public PlacementCalculation(Placement placement, Board board) {
    synchronized (calls) {
      calls.addAndGet(1);
    }

    this.placement = placement;
    this.board = board;
  }

  // Die fertige Arbeit zum Abholen in einer Queue
  public static BlockingQueue<Board> finishedCalculations = new LinkedBlockingQueue<>();

  @Override
  public void work() {
    if(board.placeBuilding(placement)) {

      if (board.getPlacedBuildings().size() < 3) {
        Work.workToDo.add(new PlacementsInTurn(
            board,
            placement.building().getColor() == Color.Blue ? Color.Black : placement.building().getColor().opponent()
        ));
      } else {
        finishedCalculations.add(board);
      }
    }
    synchronized (calls) {
      calls.addAndGet(-1);
    }
  }
}

