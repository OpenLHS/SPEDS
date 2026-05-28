package ca.griis.speds.integration.presentation;

import ca.griis.speds.presentation.api.PresentationHostEvent;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PreEvent implements PresentationHostEvent {
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
