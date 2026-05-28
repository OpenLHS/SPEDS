/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ProtocolEventhandler.
 * @brief @~english Implementation of the ProtocolEventhandler class.
 */

package ca.griis.speds.link.internal.event;

import ca.griis.speds.communication.protocol.ProtocolHostEvent;
import ca.griis.speds.communication.protocol.unit.ProtocolIdu;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details
 *      «Detailed description of the component (optional)»
 * @par Model
 *      «Model (Abstract, automation, etc.) (optional)»
 * @par Conception
 *      «Conception description (criteria and constraints) (optional)»
 * @par Limits
 *      «Limits description (optional)»
 *
 * @brief @~french Écoute les événements de la couche liaison et notifie l'instance de classe qui
 *        s'est enregistré à ces événements.
 * @par Details
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2026-02-18 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class ProtocolEventhandler implements ProtocolHostEvent {
  private final AtomicReference<ProtocolHostEvent> hostEventConsumer;

  public ProtocolEventhandler() {
    this.hostEventConsumer = new AtomicReference<>();
  }

  public Boolean register(ProtocolHostEvent subscriber) {
    return hostEventConsumer.compareAndSet(null, subscriber);
  }

  @Override
  public void notifyIdu(ProtocolIdu idu) {
    ProtocolHostEvent current = hostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered");
    }

    current.notifyIdu(idu);
  }

  @Override
  public void notifyException(Exception exception) {
    ProtocolHostEvent current = hostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered");
    }

    current.notifyException(exception);
  }
}
