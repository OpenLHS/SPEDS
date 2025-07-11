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

package ca.griis.speds.session.internal.handler.initiator;

import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.HandlerRegistry;
import ca.griis.speds.session.internal.handler.MessageHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.service.seal.SealVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

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

  public InitiatorHandlerRegistry(ObjectMapper sharedMapper, PgaService pgaService,
      BlockingQueue<ExpandedSidu> messageWeHandle,
      Map<SessionId, SessionInformation> sessionInformations) {
    handlers = List.of(
        new SesPubRecHandler(sharedMapper, pgaService, messageWeHandle, sessionInformations),
        new SesSakRecHandler(sharedMapper, pgaService, messageWeHandle, sessionInformations),
        new SesCleRecHandler(sharedMapper, messageWeHandle, sessionInformations,
            new SealVerifier()),
        new SesMsgRecHandler(sharedMapper, messageWeHandle, sessionInformations),
        new SesFinRecHandler(sharedMapper, messageWeHandle, sessionInformations));
  }

  @Override
  public List<MessageHandler> getHandlers() {
    return List.copyOf(handlers);
  }
}
