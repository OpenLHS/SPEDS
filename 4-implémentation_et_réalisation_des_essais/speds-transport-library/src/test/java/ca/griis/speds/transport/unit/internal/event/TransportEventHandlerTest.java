package ca.griis.speds.transport.unit.internal.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.griis.speds.network.api.NetworkHostEvent;
import ca.griis.speds.transport.internal.event.TransportEventHandler;
import org.junit.jupiter.api.Test;

/**
 * "Description brève du composant (classe, interface, ...)"
 *
 * <h3>Historique</h3>
 * <p>
 * XXXX-XX-XX [AS] - Implémentation initiale<br>
 * </p>
 *
 * <h3>Tâches</h3>
 * S.O.
 *
 * @author [AS] ameni.souid@usherbrooke.ca
 * @since
 */
public class TransportEventHandlerTest {
  @Test
  void register_firstSubscriber_shouldReturnTrue() {
    TransportEventHandler handler = new TransportEventHandler();

    NetworkHostEvent sub = mock(NetworkHostEvent.class);

    assertTrue(handler.register(sub));
  }

  @Test
  void register_secondSubscriber_shouldReturnFalse_andKeepFirst() {
    TransportEventHandler handler = new TransportEventHandler();

    NetworkHostEvent first = mock(NetworkHostEvent.class);
    NetworkHostEvent second = mock(NetworkHostEvent.class);

    assertTrue(handler.register(first));
    assertFalse(handler.register(second));

    // ensure it still forwards to the first one
    handler.notifyIdu("IDU");
    verify(first, times(1)).notifyIdu("IDU");
    verifyNoInteractions(second);
  }

  @Test
  void notifyIdu_whenNoSubscriber_shouldThrow() {
    TransportEventHandler handler = new TransportEventHandler();

    RuntimeException ex = assertThrows(RuntimeException.class, () -> handler.notifyIdu("IDU"));
    assertEquals("No host event is registered ", ex.getMessage());
  }

  @Test
  void notifyIdu_whenSubscriberRegistered_shouldForward() {
    TransportEventHandler handler = new TransportEventHandler();

    NetworkHostEvent sub = mock(NetworkHostEvent.class);
    handler.register(sub);

    handler.notifyIdu("IDU-123");

    verify(sub, times(1)).notifyIdu("IDU-123");
  }
}
