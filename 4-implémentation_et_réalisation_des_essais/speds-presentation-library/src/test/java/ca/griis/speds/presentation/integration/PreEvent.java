package ca.griis.speds.presentation.integration;

import ca.griis.speds.presentation.api.PresentationHostEvent;

public class PreEvent implements PresentationHostEvent {
  private String result;

  public String getResult() {
    return result;
  }

  @Override
  public void notifyIdu(String result) {
    this.result = result;
  }

  @Override
  public void notifyException(Exception exception) {}
}
