package org.work;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Generische Arbeit
public interface Work {

  // Queue um die Arbeit zu übergeben
  // muss threadsafe sein, da mehrere Threads gleichzeitig schreiben und lesen
  public static BlockingQueue<Work> workToDo = new LinkedBlockingQueue<>();

  // die Arbeit verrichten
  void work();
}
