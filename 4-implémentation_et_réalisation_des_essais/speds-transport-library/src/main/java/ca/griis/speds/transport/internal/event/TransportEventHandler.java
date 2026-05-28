/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe TransportEventHandler.
 * @brief @~english TransportEventHandler class implementation.
 */

package ca.griis.speds.transport.internal.event;

import ca.griis.speds.network.api.NetworkHostEvent;
import java.util.concurrent.atomic.AtomicReference;

public final class TransportEventHandler implements NetworkHostEvent {
  private final AtomicReference<NetworkHostEvent> networkHostEventConsumer;

  public TransportEventHandler() {
    this.networkHostEventConsumer = new AtomicReference<>();
  }

  public Boolean register(NetworkHostEvent subscriber) {
    return networkHostEventConsumer.compareAndSet(null, subscriber);
  }

  @Override
  public void notifyIdu(String idu) {
    NetworkHostEvent current = networkHostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered ");
    }
    current.notifyIdu(idu);
  }

  @Override
  public void notifyException(Exception exception) {
    NetworkHostEvent current = networkHostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered ");
    }
    current.notifyException(exception);
  }
}
