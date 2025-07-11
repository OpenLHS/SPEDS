package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.CipherException;
import org.junit.jupiter.api.Test;

class CipherExceptionTest {

  @Test
  void testCipherExceptionString() {
    String message = "A message";
    CipherException exception = new CipherException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testCipherExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    CipherException exception = new CipherException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testCipherExceptionStringThrowable() {
    // Given
    String message = "Erreur d'encryption";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    CipherException exception = new CipherException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
