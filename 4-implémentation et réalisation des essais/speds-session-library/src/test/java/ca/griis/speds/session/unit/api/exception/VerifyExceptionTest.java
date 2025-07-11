package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.VerifyException;
import org.junit.jupiter.api.Test;

class VerifyExceptionTest {

  @Test
  void testVerifyExceptionString() {
    String message = "A message";
    VerifyException exception = new VerifyException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testVerifyExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    VerifyException exception = new VerifyException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testVerifyExceptionStringThrowable() {
    // Given
    String message = "Erreur d'encryption";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    VerifyException exception = new VerifyException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
