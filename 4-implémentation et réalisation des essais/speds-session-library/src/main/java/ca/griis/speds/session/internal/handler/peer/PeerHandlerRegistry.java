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

package ca.griis.speds.session.internal.handler.peer;

import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.HandlerRegistry;
import ca.griis.speds.session.internal.handler.MessageHandler;
import ca.griis.speds.session.internal.model.PendingIndication;
import ca.griis.speds.session.internal.model.SessionInformation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
public class PeerHandlerRegistry implements HandlerRegistry {

  private final List<MessageHandler> handlers;

  public PeerHandlerRegistry(HostStartupContext ctx, BlockingQueue<Pidu> queue,
      Map<SessionId, SessionInformation> sessionInfo,
      ConcurrentLinkedQueue<PendingIndication> indicationTracking) {
    handlers = List.of(
        new SesPubEnvHandler(ctx.spedsDto(), ctx.transportHost(), ctx.sharedMapper(), sessionInfo,
            ctx.hostKeys()),
        new SesSakEnvHandler(ctx.spedsDto(), ctx.sharedMapper(), ctx.transportHost(),
            ctx.hostKeys(),
            sessionInfo),
        new SesCleEnvHandler(ctx.spedsDto(), ctx.sharedMapper(), ctx.pgaService(),
            ctx.transportHost(),
            sessionInfo),
        new SesMsgEnvHandler(ctx.sharedMapper(), sessionInfo, indicationTracking, queue),
        new SesFinEnvHandler(ctx.spedsDto(), ctx.sharedMapper(), ctx.transportHost(), sessionInfo));
  }

  @Override
  public List<MessageHandler> getHandlers() {
    return List.copyOf(handlers);
  }
}
