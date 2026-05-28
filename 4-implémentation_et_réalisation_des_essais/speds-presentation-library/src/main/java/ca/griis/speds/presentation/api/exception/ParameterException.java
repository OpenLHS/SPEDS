package ca.griis.speds.presentation.api.exception;

public class ParameterException extends RuntimeException {
  private static final long serialVersionUID = -8089659852369914868L;

  public ParameterException(String message) {
    super(message);
  }
}
