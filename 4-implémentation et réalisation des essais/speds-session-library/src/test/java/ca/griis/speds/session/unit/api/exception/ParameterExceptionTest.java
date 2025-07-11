package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.ParameterException;
import org.junit.jupiter.api.Test;

public class ParameterExceptionTest {

  @Test
  public void testParameterExceptionString() {
    String message = "A message";
    ParameterException exception = new ParameterException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  public void testParameterExceptionStringThrowable() {
    // Given
    String message = "Erreur de paramètres";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    ParameterException exception = new ParameterException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
