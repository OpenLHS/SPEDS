/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SubmitDispatcher.
 * @brief @~english Contains description of SubmitDispatcher class.
 */

package ca.griis.speds.network.internal.dispatcher;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.VersionDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.network.internal.handler.TransferRequestHandler;
import ca.griis.speds.network.internal.handler.TransferResponseHandler;
import ca.griis.speds.network.internal.identification.IdentifierGenerator;
import ca.griis.speds.network.internal.security.CertificatePrivateKeyPair;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
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
 * @brief @~french Assigne un IDU soumissionné et qui est associée à une primitive de service au bon
 *        gestionnaire de soumission.
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
public final class SubmitDispatcher {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(SubmitDispatcher.class);

  private final TransferRequestHandler transferRequestHandler;
  private final TransferResponseHandler transferResponseHandler;
  private final ObjectMapper objectMapper;

  public SubmitDispatcher(IdentifierGenerator generator, VersionDto version,
      CertificatePrivateKeyPair pair, CryptographyService service, ObjectMapper objectMapper,
      Host host, Map<UUID, Boolean> indicatedMessages) {
    this.transferRequestHandler =
        new TransferRequestHandler(generator, version, pair, service, objectMapper, host);
    this.objectMapper = objectMapper;
    this.transferResponseHandler =
        new TransferResponseHandler(objectMapper, host, indicatedMessages);
  }

  public Optional<String> handle(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Optional<String> nidu = Optional.empty();

    try {
      final var nsdu = objectMapper.readValue(idu, InterfaceDataUnit45Dto.class);
      final var context = nsdu.getContext();

      if (context.getService().equals("transfer") == false) {
        final var ex = new RuntimeException("FAILED: Unknown primitive service");
        logger.error(Error.IGNORED_ERROR, ex);
      } else if (context.getServicePrimitive() == Context45Dto.ServicePrimitive.REQUEST) {
        nidu = transferRequestHandler.handle(nsdu);
      } else if (context.getServicePrimitive() == Context45Dto.ServicePrimitive.RESPONSE) {
        nidu = transferResponseHandler.handle(nsdu);
      } else {
        final var ex = new RuntimeException("FAILED: Unknown primitive service");
        logger.error(Error.IGNORED_ERROR, ex);
      }
    } catch (Exception e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "nidu", nidu);
    return nidu;
  }
}
