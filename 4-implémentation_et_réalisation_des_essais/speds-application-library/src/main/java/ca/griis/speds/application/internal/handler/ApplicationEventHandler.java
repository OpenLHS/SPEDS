/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ApplicationEventHandler.
 * @brief @~english ApplicationEventHandler class implementation.
 */

package ca.griis.speds.application.internal.handler;

import ca.griis.speds.presentation.api.PresentationHostEvent;
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
 * @brief @~french «Brève description de la composante (classe, interface, ...)»
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2026-03-04 [CB] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class ApplicationEventHandler implements PresentationHostEvent {
  private final AtomicReference<PresentationHostEvent> hostEventConsumer;

  public ApplicationEventHandler() {
    this.hostEventConsumer = new AtomicReference<>();
  }

  public Boolean register(PresentationHostEvent subscriber) {
    return hostEventConsumer.compareAndSet(null, subscriber);
  }

  @Override
  public void notifyIdu(String idu) {
    PresentationHostEvent current = hostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered");
    }

    current.notifyIdu(idu);
  }

  @Override
  public void notifyException(Exception exception) {
    PresentationHostEvent current = hostEventConsumer.get();
    if (current == null) {
      throw new RuntimeException("No host event is registered");
    }

    current.notifyException(exception);
  }
}
