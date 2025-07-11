package ca.griis.speds.presentation.unit.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import ca.griis.speds.presentation.api.exception.CipherException;
import org.junit.jupiter.api.Test;

public class CipherExceptionTest {

  @Test
  void testNoArgsConstructor() {
    // Given
    CipherException exception = new CipherException();

    // Then
    assertNotNull(exception, "L'exception ne doit pas être nulle.");
    assertNull(exception.getMessage(), "Le message ne doit pas être défini.");
  }

  @Test
  void testMessageConstructor() {
    // Given
    String message = "Erreur de chiffrement";
    CipherException exception = new CipherException(message);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testMessageAndCauseConstructor() {
    // Given
    String message = "Erreur de chiffrement";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    CipherException exception = new CipherException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testCauseConstructor() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    CipherException exception = new CipherException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause.toString(), exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
