package ca.griis.speds.session.integration.concurency;

import java.util.concurrent.CyclicBarrier;

// Version générique pour N tâches distinctes
public class MultipleTaskTrigger {

  public void execute(Runnable... tasks) throws InterruptedException {
    if (tasks.length == 0)
      return;

    CyclicBarrier barrier = new CyclicBarrier(tasks.length);
    Thread[] threads = new Thread[tasks.length];

    for (int i = 0; i < tasks.length; i++) {
      final Runnable task = tasks[i];
      threads[i] = new Thread(() -> {
        try {
          barrier.await();
          task.run();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      threads[i].start();
    }

    for (Thread thread : threads) {
      thread.join();
    }
  }
}
