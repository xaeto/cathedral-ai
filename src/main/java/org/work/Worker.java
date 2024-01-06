package org.work;

import java.util.concurrent.TimeUnit;

public class Worker extends Thread{
  private final String name;
  public Worker(String name) {
    super(name);
    this.name = name;
  }

  // Um den Worker zu stoppen,
  // volatile damit Änderungen sofort in allen Threads sichtbar werden
  private volatile boolean stop = false;

  // Beenden des Arbeiters
  public void fire() {
    this.stop = true;
  }

  @Override
  public void run() {
    while(!stop){
      try {
        // auf arbeit warten
        // falls zu lange gewartet schleife schauen ob überhaupt noch arbeiter ist
        Work work = Work.workToDo.poll(10, TimeUnit.MILLISECONDS);
        if(work != null){
          work.work();
        }
      } catch (InterruptedException e) {
        System.out.println(name + "has been interrupted!");
      }
    }
  }
}
