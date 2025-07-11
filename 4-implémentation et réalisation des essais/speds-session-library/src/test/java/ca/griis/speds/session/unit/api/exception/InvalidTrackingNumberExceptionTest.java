package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.InvalidTrackingNumberException;
import org.junit.jupiter.api.Test;

public class InvalidTrackingNumberExceptionTest {

  @Test
  public void testInvalidTrackingNumberExceptionString() {
    String message = "A message";
    InvalidTrackingNumberException exception = new InvalidTrackingNumberException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  public void testInvalidTrackingNumberExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    InvalidTrackingNumberException exception = new InvalidTrackingNumberException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  public void testInvalidTrackingNumberExceptionStringThrowable() {
    // Given
    String message = "Erreur de clé";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    InvalidTrackingNumberException exception = new InvalidTrackingNumberException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
