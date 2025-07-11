package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;

class InvalidTokenExceptionTest {

  @Test
  void testInvalidTokenExceptionString() {
    String message = "A message";
    InvalidTokenException exception = new InvalidTokenException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testInvalidTokenExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    InvalidTokenException exception = new InvalidTokenException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testInvalidTokenExceptionStringThrowable() {
    // Given
    String message = "Erreur d'encryption";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    InvalidTokenException exception = new InvalidTokenException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
