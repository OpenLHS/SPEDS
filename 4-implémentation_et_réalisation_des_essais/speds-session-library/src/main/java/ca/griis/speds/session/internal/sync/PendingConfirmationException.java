
package ca.griis.speds.session.internal.sync;

public class PendingConfirmationException extends Exception {
  private static final long serialVersionUID = 1813807707409882397L;

  public PendingConfirmationException(String message) {
    super(message);
  }

  public PendingConfirmationException(Throwable cause) {
    super(cause);
  }

  public PendingConfirmationException(String message, Throwable cause) {
    super(message, cause);
  }
}
