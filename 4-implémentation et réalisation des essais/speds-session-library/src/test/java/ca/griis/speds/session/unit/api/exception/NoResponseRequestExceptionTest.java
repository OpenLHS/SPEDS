package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.NoResponseRequestException;
import org.junit.jupiter.api.Test;

public class NoResponseRequestExceptionTest {

  @Test
  public void testNoResponseRequestExceptionString() {
    String message = "A message";
    NoResponseRequestException exception = new NoResponseRequestException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  public void testNoResponseRequestExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    NoResponseRequestException exception = new NoResponseRequestException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  public void testNoResponseRequestExceptionStringThrowable() {
    // Given
    String message = "Erreur de clé";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    NoResponseRequestException exception = new NoResponseRequestException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
