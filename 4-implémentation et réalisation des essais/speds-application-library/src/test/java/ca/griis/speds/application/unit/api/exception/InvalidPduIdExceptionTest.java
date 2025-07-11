package ca.griis.speds.application.unit.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.griis.speds.application.api.exception.InvalidPduIdException;
import org.junit.jupiter.api.Test;

public class InvalidPduIdExceptionTest {

  @Test
  void testInvalidPduIdException() {
    InvalidPduIdException exception = new InvalidPduIdException();

    assertNotNull(exception);
    assertNull(exception.getMessage());
  }

  @Test
  void testInvalidPduIdExceptionString() {
    String message = "an exception";
    InvalidPduIdException exception = new InvalidPduIdException(message);

    assertNotNull(exception);
    assertEquals(exception.getMessage(), message);
  }

  @Test
  void testInvalidPduIdExceptionStringThrowable() {
    // Given
    String message = "Erreur de pduId";
    Throwable cause = new RuntimeException("Cause de l'erreur");
    InvalidPduIdException exception = new InvalidPduIdException(message, cause);

    // Then
    assertNotNull(exception);
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

}
