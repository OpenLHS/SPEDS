package ca.griis.speds.session.unit.internal.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.internal.processing.SilentIgnoreException;
import org.junit.jupiter.api.Test;

class SilentIgnoreExceptionTest {

  @Test
  void testSilentIgnoreExceptionString() {
    String message = "A message";
    SilentIgnoreException exception = new SilentIgnoreException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testSilentIgnoreExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    SilentIgnoreException exception = new SilentIgnoreException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testSilentIgnoreExceptionStringThrowable() {
    // Given
    String message = "Erreur d'encryption";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    SilentIgnoreException exception = new SilentIgnoreException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
