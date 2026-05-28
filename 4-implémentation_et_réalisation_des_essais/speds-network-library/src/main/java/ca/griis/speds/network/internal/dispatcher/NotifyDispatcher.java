/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe NotifyDispatcher.
 * @brief @~english Contains description of NotifyDispatcher class.
 */

package ca.griis.speds.network.internal.dispatcher;

import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.network.internal.handler.TransferIndicationHandler;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
 * @brief @~french Assigne un IDU reçu par événement et qui est associée à une primitive de service
 *        au bon gestionnaire de notification.
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
public final class NotifyDispatcher {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(NotifyDispatcher.class);

  private final TransferIndicationHandler transferIndicationHandler;

  public NotifyDispatcher(CryptographyService service, ObjectMapper objectMapper, Host host,
      Map<UUID, Boolean> indicatedMessages) {
    this.transferIndicationHandler =
        new TransferIndicationHandler(service, objectMapper, host, indicatedMessages);
  }

  public Optional<String> handle(InterfaceDataUnit56Dto idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Optional<String> nidu = Optional.empty();

    try {
      final var context = idu.getContext();
      if (context.getService().equals("transfer") == false) {
        final var ex = new RuntimeException("FAILED: Unknown primitive service");
        logger.error(Error.IGNORED_ERROR, ex);
      } else if (context.getServicePrimitive() == Context56Dto.ServicePrimitive.INDICATION) {
        nidu = transferIndicationHandler.handle(idu);
      } else {
        final var ex = new RuntimeException("FAILED: Unknown primitive service");
        logger.error(Error.IGNORED_ERROR, ex);
      }
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "nidu", nidu);
    return nidu;
  }
}
