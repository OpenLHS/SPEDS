package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.GetPublicKeyException;
import org.junit.jupiter.api.Test;

class GetPublicKeyExceptionTest {

  @Test
  void testGetPublicKeyExceptionString() {
    String message = "A message";
    GetPublicKeyException exception = new GetPublicKeyException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testGetPublicKeyExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    GetPublicKeyException exception = new GetPublicKeyException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testGetPublicKeyExceptionStringThrowable() {
    // Given
    String message = "Erreur d'encryption";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    GetPublicKeyException exception = new GetPublicKeyException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
