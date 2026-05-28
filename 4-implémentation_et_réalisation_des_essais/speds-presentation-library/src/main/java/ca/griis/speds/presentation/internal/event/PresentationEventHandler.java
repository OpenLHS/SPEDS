

/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe PresentationEventHandler.
 * @brief @~english PresentationEventHandler class implementation.
 */

package ca.griis.speds.presentation.internal.event;

import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.internal.domain.SessionId;
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
 * @brief @~french Écoute les événements de la couche session et notifie l'instance de classe qui
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
 *      2026-04-24 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */

public final class PresentationEventHandler implements SessionHostEvent {
  private final AtomicReference<SessionHostEvent> hostEventConsumer;

  public PresentationEventHandler() {
    this.hostEventConsumer = new AtomicReference<>();
  }

  public Boolean register(SessionHostEvent subscriber) {
    return hostEventConsumer.compareAndSet(null, subscriber);
  }

  @Override
  public void notifyIdu(String idu) {
    SessionHostEvent current = hostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered");
    }

    current.notifyIdu(idu);
  }


  @Override
  public void notifyException(Exception exception) {
    SessionHostEvent current = hostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered");
    }

    current.notifyException(exception);
  }

  @Override
  public void notifyInitiatorSessionTerminatedSuccessfully(SessionId sessionId) {
    SessionHostEvent current = hostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered");
    }

    current.notifyInitiatorSessionTerminatedSuccessfully(sessionId);
  }

  @Override
  public void notifyPeerSessionTerminatedSuccessfully(SessionId sessionId) {
    SessionHostEvent current = hostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered");
    }

    current.notifyPeerSessionTerminatedSuccessfully(sessionId);
  }
}
