package ca.griis.speds.transport.integration;

import ca.griis.speds.transport.api.TransportHostEvent;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TraEvent implements TransportHostEvent {
  private final LinkedBlockingQueue<String> clientIdus = new LinkedBlockingQueue<>();

  public String getResult(Long times) throws InterruptedException {
    return clientIdus.poll(times, TimeUnit.SECONDS);
  }

  @Override
  public void notifyIdu(String idu) {
    clientIdus.add(idu);
  }

  @Override
  public void notifyException(Exception exception) {}
}
