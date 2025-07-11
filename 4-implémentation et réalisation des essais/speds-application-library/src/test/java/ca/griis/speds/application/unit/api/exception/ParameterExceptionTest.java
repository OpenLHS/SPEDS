package ca.griis.speds.application.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.application.api.exception.ParameterException;
import org.junit.jupiter.api.Test;

public class ParameterExceptionTest {

  @Test
  void testParameterExceptionString() {
    String message = "an exception";
    ParameterException exception = new ParameterException(message);

    assertNotNull(exception);
    assertEquals(exception.getMessage(), message);
  }

  @Test
  void testParameterExceptionStringThrowable() {
    // Given
    String message = "Erreur de parametres";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    ParameterException exception = new ParameterException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

}
