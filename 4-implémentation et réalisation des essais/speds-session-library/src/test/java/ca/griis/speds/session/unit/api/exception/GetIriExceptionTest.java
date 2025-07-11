package ca.griis.speds.session.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.griis.speds.session.api.exception.GetIriException;
import org.junit.jupiter.api.Test;

class GetIriExceptionTest {

  @Test
  void testGetIriExceptionString() {
    String message = "A message";
    GetIriException exception = new GetIriException(message);

    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
  }

  @Test
  void testGetIriExceptionThrowable() {
    // Given
    Throwable cause = new RuntimeException("Cause de l'erreur");
    GetIriException exception = new GetIriException(cause);

    // Then
    assertNotNull(exception);
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testGetIriExceptionStringThrowable() {
    // Given
    String message = "Erreur d'encryption";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    GetIriException exception = new GetIriException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
