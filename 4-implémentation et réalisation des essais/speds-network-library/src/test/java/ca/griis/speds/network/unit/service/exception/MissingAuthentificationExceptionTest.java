package ca.griis.speds.network.unit.service.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.network.service.exception.MissingAuthenticationException;
import org.junit.jupiter.api.Test;

public class MissingAuthentificationExceptionTest {

  @Test
  public void messageConstructorTest() throws Exception {
    final String message = "exception message";
    final MissingAuthenticationException exception = new MissingAuthenticationException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  public void messageCauseConstructorTest() throws Exception {
    final String message = "exception message";
    final Throwable cause = new RuntimeException("cause");
    final MissingAuthenticationException exception =
        new MissingAuthenticationException(message, cause);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
