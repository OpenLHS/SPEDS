/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SessionEventHandler.
 * @brief @~english SessionEventHandler class implementation.
 */

package ca.griis.speds.session.internal.event;

import ca.griis.speds.transport.api.TransportHostEvent;
import java.util.concurrent.atomic.AtomicReference;

public final class SessionEventHandler implements TransportHostEvent {
  private final AtomicReference<TransportHostEvent> transportHostEventConsumer;

  public SessionEventHandler() {
    this.transportHostEventConsumer = new AtomicReference<>();
  }

  public Boolean register(TransportHostEvent subscriber) {
    return transportHostEventConsumer.compareAndSet(null, subscriber);
  }

  @Override
  public void notifyIdu(String idu) {
    TransportHostEvent current = transportHostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered ");
    }
    current.notifyIdu(idu);
  }

  @Override
  public void notifyException(Exception exception) {
    TransportHostEvent current = transportHostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered ");
    }
    current.notifyException(exception);
  }
}
