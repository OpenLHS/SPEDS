package ca.griis.speds.presentation.unit.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import ca.griis.speds.presentation.api.exception.DeserializationException;
import org.junit.jupiter.api.Test;

public class DeserializationExceptionTest {

  @Test
  void testNoArgsConstructor() {
    // Given
    DeserializationException exception = new DeserializationException();

    // Then
    assertNotNull(exception, "L'exception ne doit pas être nulle.");
    assertNull(exception.getMessage(), "Le message ne doit pas être défini.");
  }

  @Test
  void testMessageConstructor() {
    // Given
    String message = "Erreur de chiffrement";
    DeserializationException exception = new DeserializationException(message);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testMessageAndCauseConstructor() {
    // Given
    String message = "Erreur de chiffrement";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    DeserializationException exception = new DeserializationException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testCauseConstructor() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    DeserializationException exception = new DeserializationException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause.toString(), exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
