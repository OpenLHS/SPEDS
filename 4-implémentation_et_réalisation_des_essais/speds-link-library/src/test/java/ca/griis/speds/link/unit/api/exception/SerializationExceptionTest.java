package ca.griis.speds.link.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.griis.speds.link.api.exception.SerializationException;
import org.junit.jupiter.api.Test;

class SerializationExceptionTest {

  @Test
  void testSerializationException() {
    SerializationException exception = new SerializationException();

    assertNotNull(exception);
    assertNull(exception.getMessage());
  }

  @Test
  void testSerializationExceptionString() {
    String message = "an exception";
    SerializationException exception = new SerializationException(message);

    assertNotNull(exception);
    assertEquals(exception.getMessage(), message);
  }

  @Test
  void testSerializationExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    SerializationException exception = new SerializationException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause.toString(), exception.getMessage());
    assertEquals(cause, exception.getCause());

  }

  @Test
  void testSerializationExceptionStringThrowable() {
    // Given
    String message = "Erreur de serialisation";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    SerializationException exception = new SerializationException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
