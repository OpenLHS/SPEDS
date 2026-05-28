/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe TransferIndicationHandler.
 * @brief @~english Contains description of TransferIndicationHandler class.
 */

package ca.griis.speds.network.internal.handler;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5NETDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.network.internal.checker.NetworkPduChecker;
import ca.griis.speds.network.internal.checker.PduCheckerResult;
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
 * @brief @~french Traite un transfert d'une indication.
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
public final class TransferIndicationHandler {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(TransferIndicationHandler.class);

  private final ObjectMapper objectMapper;
  private final NetworkPduChecker checker;
  private final Host host;
  private final Map<UUID, Boolean> indicatedMessages;

  public TransferIndicationHandler(CryptographyService service, ObjectMapper objectMapper,
      Host host, Map<UUID, Boolean> indicatedMessages) {
    this.objectMapper = objectMapper;
    this.checker = new NetworkPduChecker(service, objectMapper);
    this.host = host;
    this.indicatedMessages = indicatedMessages;
  }

  public Optional<String> handle(InterfaceDataUnit56Dto idu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Optional<String> result = Optional.empty();
    ProtocolDataUnit5NETDto pdu = null;
    PduCheckerResult checkResult;
    try {
      pdu = objectMapper.readValue(idu.getMessage(), ProtocolDataUnit5NETDto.class);
      checkResult = checker.check(pdu, idu.getContext().getDestinationIri());
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);

      checkResult = new PduCheckerResult(false, "FAILED: JSON error");
    }

    if (pdu != null) {
      if (checkResult.isValid()) {
        final var headerDto = pdu.getHeader();
        final var msgId = pdu.getHeader().getId();
        final var options = Map.of("TN", msgId, "IRI", headerDto.getDestinationIri());
        var ici =
            new Context45Dto(headerDto.getSourceIri(), headerDto.getDestinationIri(),
                "transfer", Context45Dto.ServicePrimitive.INDICATION, options);
        var nsdu = new InterfaceDataUnit45Dto(ici, pdu.getContent());
        result = Optional.of(objectMapper.writeValueAsString(nsdu));

        indicatedMessages.putIfAbsent(msgId, true);
      } else {
        final var headerDto = pdu.getHeader();
        final var nici =
            new Context56Dto(headerDto.getSourceIri(), "transfer",
                Context56Dto.ServicePrimitive.RESPONSE, false);
        final var niduDto = new InterfaceDataUnit56Dto(nici, checkResult.message());
        final var nidu = objectMapper.writeValueAsString(niduDto);

        host.submitIdu(nidu);
      }
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
