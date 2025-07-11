package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.KeyAgreementException;
import org.junit.jupiter.api.Test;

public class KeyAgreementExceptionTest {

  @Test
  public void testKeyAgreementExceptionString() {
    String message = "A message";
    KeyAgreementException exception = new KeyAgreementException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  public void testKeyAgreementExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    KeyAgreementException exception = new KeyAgreementException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  public void testKeyAgreementExceptionStringThrowable() {
    // Given
    String message = "Erreur de clé";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    KeyAgreementException exception = new KeyAgreementException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
