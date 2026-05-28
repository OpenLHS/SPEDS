package ca.griis.speds.network.integration;

import ca.griis.speds.network.api.NetworkHostEvent;

public class NetEvent implements NetworkHostEvent {
  private String result;

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  @Override
  public void notifyIdu(String idu) {
    this.result = idu;
  }

  @Override
  public void notifyException(Exception exception) {}
}
