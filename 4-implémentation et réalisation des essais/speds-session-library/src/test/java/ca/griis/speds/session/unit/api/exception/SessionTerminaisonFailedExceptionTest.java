package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.SessionTerminaisonFailedException;
import org.junit.jupiter.api.Test;

public class SessionTerminaisonFailedExceptionTest {

  @Test
  public void testSessionTerminaisonFailedExceptionString() {
    String message = "A message";
    SessionTerminaisonFailedException exception = new SessionTerminaisonFailedException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  public void testSessionTerminaisonFailedExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    SessionTerminaisonFailedException exception = new SessionTerminaisonFailedException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  public void testSessionTerminaisonFailedExceptionStringThrowable() {
    // Given
    String message = "Erreur de clé";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    SessionTerminaisonFailedException exception =
        new SessionTerminaisonFailedException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
