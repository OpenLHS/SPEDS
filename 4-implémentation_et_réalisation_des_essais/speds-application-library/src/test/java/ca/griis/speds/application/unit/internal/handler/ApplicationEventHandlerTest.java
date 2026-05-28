package ca.griis.speds.application.unit.internal.handler;

import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.speds.application.internal.handler.ApplicationEventHandler;
import org.junit.jupiter.api.Test;

public class ApplicationEventHandlerTest {

  @Test
  public void notifyIduTest() throws Exception {
    ApplicationEventHandler applicationEventHandler = new ApplicationEventHandler();
    assertThrows(RuntimeException.class, () -> applicationEventHandler.notifyIdu("idu"));
  }
}
