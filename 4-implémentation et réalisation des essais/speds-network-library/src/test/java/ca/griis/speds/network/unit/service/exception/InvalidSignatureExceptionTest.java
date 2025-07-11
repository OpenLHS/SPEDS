package ca.griis.speds.network.unit.service.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.network.service.exception.InvalidSignatureException;
import org.junit.jupiter.api.Test;

public class InvalidSignatureExceptionTest {

  @Test
  public void messageConstructorTest() throws Exception {
    final String message = "exception message";
    final InvalidSignatureException exception = new InvalidSignatureException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  public void messageCauseConstructorTest() throws Exception {
    final String message = "exception message";
    final Throwable cause = new RuntimeException("cause");
    final InvalidSignatureException exception = new InvalidSignatureException(message, cause);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
