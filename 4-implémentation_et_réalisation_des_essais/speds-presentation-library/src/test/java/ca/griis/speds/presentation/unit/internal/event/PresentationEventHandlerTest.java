package ca.griis.speds.presentation.unit.internal.event;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import ca.griis.speds.presentation.internal.event.PresentationEventHandler;
import ca.griis.speds.session.api.SessionHostEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PresentationEventHandlerTest {

  private PresentationEventHandler handler;

  @Mock
  private SessionHostEvent mockSubscriber;

  @Mock
  private SessionHostEvent mockSubscriber2;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    handler = new PresentationEventHandler();
  }

  @Test
  void testRegisterFirstSubscriberReturnsTrue() {
    assertTrue(handler.register(mockSubscriber));
  }

  @Test
  void testRegisterSecondSubscriberReturnsFalse() {
    handler.register(mockSubscriber);
    assertFalse(handler.register(mockSubscriber2));
  }

  @Test
  void testNotifyIduDelegatesToSubscriber() {
    handler.register(mockSubscriber);
    handler.notifyIdu("test message");
    verify(mockSubscriber).notifyIdu("test message");
  }

  @Test
  void testNotifyIduWithoutSubscriberThrowsException() {
    assertThrows(RuntimeException.class, () -> {
      handler.notifyIdu("test message");
    });
  }
}
