package org.work;


import de.fhkiel.ki.cathedral.game.Color;
import de.fhkiel.ki.cathedral.game.Game;
import java.util.ArrayList;
import java.util.List;

// Der Manager verteilt und nutzt die Arbeit
public class Manager {

  private final List<Worker> workers = new ArrayList<>();
  private final BeanCounter beanCounter;
  public Manager() {
    // Arbeiter einstellen
    for(int i = 1; i <= 11; ++i){
      Worker worker = new Worker("Worker " + i);
      worker.start();
      workers.add(worker);
    }
    // beanCounter einstellen
    beanCounter = new BeanCounter();
    beanCounter.start();
  }

  public void manage(Game game) throws InterruptedException {
    // Starten der Kalkulation
    Work.workToDo.add(new PlacementsInTurn(game.getBoard(), Color.Blue));

    // Solange weiterarbeiten bis nix mehr zu tun is
    do {
      Thread.sleep(100);
    } while(!Work.workToDo.isEmpty());

    System.out.println("Ende, Arbeit fertig! Anzahl an boards: " + PlacementCalculation.finishedCalculations.size());
    // Ende
    stop();
  }

  public void stop() {
    // alle feuern
    for(Worker worker : workers){
      worker.fire();
    }
    beanCounter.fire();
  }
}
