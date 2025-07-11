package ca.griis.speds.application.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.griis.speds.application.api.exception.DeserializationException;
import org.junit.jupiter.api.Test;

public class DeserializationExceptionTest {

  @Test
  void testDeserializationException() {
    DeserializationException exception = new DeserializationException();

    assertNotNull(exception);
    assertNull(exception.getMessage());
  }

  @Test
  void testDeserializationExceptionString() {
    String message = "A message";
    DeserializationException exception = new DeserializationException(message);

    assertNotNull(exception);
    assertEquals(exception.getMessage(), message);
  }

  @Test
  void testDeserializationExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    DeserializationException exception = new DeserializationException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testDeserializationExceptionStringThrowable() {
    // Given
    String message = "Erreur de deserialisation";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    DeserializationException exception = new DeserializationException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
