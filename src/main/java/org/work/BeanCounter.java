package org.work;

// Damit sachen ausgegeben werden
public class BeanCounter extends Thread{

  private volatile boolean stop = false;

  public void fire() {
    this.stop = true;
  }

  @Override
  public void run() {
    long startTime = System.currentTimeMillis();
    while (!stop){
      System.out.println((System.currentTimeMillis() - startTime) + "ms: " + Work.workToDo.size() + " Arbeitspackete und " +
          PlacementCalculation.finishedCalculations.size() + " fertige Boards (" + PlacementCalculation.calls.get() + "/" + PlacementsInTurn.calls.get() + ")");
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
