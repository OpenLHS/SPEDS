package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.DeserializationException;
import org.junit.jupiter.api.Test;

public class DeserializationExceptionTest {

  @Test
  public void testDeserializationExceptionString() {
    String message = "A message";
    DeserializationException exception = new DeserializationException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  public void testDeserializationExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    DeserializationException exception = new DeserializationException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  public void testDeserializationExceptionStringThrowable() {
    // Given
    String message = "Erreur de clé";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    DeserializationException exception = new DeserializationException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
