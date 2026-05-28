package ca.griis.speds.integration.transport;

import ca.griis.speds.transport.api.TransportHostEvent;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TraEvent implements TransportHostEvent {
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
