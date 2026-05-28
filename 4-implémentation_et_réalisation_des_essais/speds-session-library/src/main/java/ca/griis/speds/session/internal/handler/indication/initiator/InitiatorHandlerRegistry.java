/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe HandlerRegistry.
 * @brief @~english Implementation of the HandlerRegistry class.
 */

package ca.griis.speds.session.internal.handler.indication.initiator;

import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.handler.indication.HandlerRegistry;
import ca.griis.speds.session.internal.handler.indication.MessageHandler;
import java.util.List;

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
 * @brief @~french Implémentation du registre des gestionnaires de message interne.
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
 *      2025-06-29 [MD] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class InitiatorHandlerRegistry implements HandlerRegistry {

  private final List<MessageHandler> handlers;

  public InitiatorHandlerRegistry(HostStartupContext ctx, InitiatorEvent initiatorEvent) {
    handlers = List.of(
        new SesPubRecHandler(ctx, initiatorEvent),
        new SesSakRecHandler(ctx, initiatorEvent),
        new SesCleRecHandler(ctx, initiatorEvent),
        new SesMsgRecHandler(ctx, initiatorEvent),
        new SesFinRecHandler(ctx, initiatorEvent));
  }

  @Override
  public List<MessageHandler> getHandlers() {
    return List.copyOf(handlers);
  }
}
