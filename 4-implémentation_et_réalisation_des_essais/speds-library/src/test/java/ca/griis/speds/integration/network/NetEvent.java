package ca.griis.speds.integration.network;

import ca.griis.speds.network.api.NetworkHostEvent;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NetEvent implements NetworkHostEvent {
  private final LinkedBlockingQueue<String> idus = new LinkedBlockingQueue<>();

  public String poll(Long times) throws InterruptedException {
    return idus.poll(times, TimeUnit.SECONDS);
  }

  @Override
  public void notifyIdu(String idu) {
    idus.add(idu);
  }

  public Boolean isEmpty() {
    return idus.isEmpty();
  }

  @Override
  public void notifyException(Exception exception) {}
}
