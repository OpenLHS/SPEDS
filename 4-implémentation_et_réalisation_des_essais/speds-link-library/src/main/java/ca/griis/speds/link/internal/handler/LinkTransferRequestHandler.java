/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe LinkTransferRequestHandler.
 * @brief @~english Implementation of the LinkTransferRequestHandler class.
 */

package ca.griis.speds.link.internal.handler;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.link.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.communication.protocol.ProtocolHost;
import ca.griis.speds.communication.protocol.unit.ProtocolIdu;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

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
 * @brief @~french Traite un transfert d'une requête.
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
 *      2026-04-21 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class LinkTransferRequestHandler {
  private static final GriisLogger logger = getLogger(LinkTransferRequestHandler.class);

  private final ObjectMapper objectMapper;
  private final ProtocolHost host;

  public LinkTransferRequestHandler(ObjectMapper objectMapper, ProtocolHost host) {
    this.objectMapper = objectMapper;
    this.host = host;
  }

  public Optional<String> handle(InterfaceDataUnit56Dto idu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    final ContextDto ici = idu.getContext();
    final String sdu = idu.getMessage();
    final ProtocolIdu protocolIdu = new ProtocolIdu(ici.getDestinationIri(), sdu);
    host.send(protocolIdu);

    final ContextDto contextResponseDto = new ContextDto(
        idu.getContext().getDestinationIri(),
        ContextDto.Service.TRANSFER,
        ContextDto.ServicePrimitive.CONFIRM,
        idu.getContext().getOptions());

    final String confirmMessageContent = "SUCCEED";
    final InterfaceDataUnit56Dto iduConfirmationDto =
        new InterfaceDataUnit56Dto(contextResponseDto, confirmMessageContent);

    final String iduStringConfirmation = objectMapper.writeValueAsString(iduConfirmationDto);
    Optional<String> result = Optional.of(iduStringConfirmation);

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
