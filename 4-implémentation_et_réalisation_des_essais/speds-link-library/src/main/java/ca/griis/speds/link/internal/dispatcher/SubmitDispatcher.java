/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SubmitDispatcher.
 * @brief @~english Implementation of the SubmitDispatcher class.
 */

package ca.griis.speds.link.internal.dispatcher;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.link.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.link.api.dto.ContextDto.ServicePrimitive;
import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.speds.communication.protocol.ProtocolHost;
import ca.griis.speds.link.api.exception.ProtocolException;
import ca.griis.speds.link.internal.handler.LinkTransferRequestHandler;
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
 *      2026-04-21 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class SubmitDispatcher {
  private static final GriisLogger logger = getLogger(SubmitDispatcher.class);

  private final ObjectMapper objectMapper;
  private final LinkTransferRequestHandler handler;

  public SubmitDispatcher(ObjectMapper objectMapper, ProtocolHost host) {
    this.objectMapper = objectMapper;
    this.handler = new LinkTransferRequestHandler(objectMapper, host);
  }

  public Optional<String> handle(String idu) {
    Optional<String> result = Optional.empty();

    try {
      InterfaceDataUnit56Dto inIdu = objectMapper.readValue(idu, InterfaceDataUnit56Dto.class);
      ContextDto context = inIdu.getContext();
      var service = context.getService();
      if (service != ContextDto.Service.TRANSFER) {
        final var failed = "FAILED: Unknown service";
        final var ex = new RuntimeException(failed);
        logger.error(Error.IGNORED_ERROR, ex);

        result = createConfirm(failed);
      } else if (context.getServicePrimitive() == ServicePrimitive.REQUEST) {
        result = handler.handle(inIdu);
      } else if (context.getServicePrimitive() == ServicePrimitive.RESPONSE) {
        final String responseMessage = inIdu.getMessage();
        logger.info(Info.VARIABLE_LOGGING_1, "responseMessage", responseMessage);
      } else {
        final var failed = "FAILED: Unknown primitive service";
        logger.error(Error.IGNORED_ERROR, new ProtocolException(failed));

        result = createConfirm(failed);
      }
    } catch (Exception ex) {
      logger.error(Error.IGNORED_ERROR, ex);

      result = createConfirm("FAILED: Unable to deliver to the destination");
    }

    return result;
  }

  private Optional<String> createConfirm(String confirmMessageContent) {
    final ContextDto contextResponseDto = new ContextDto("?", ContextDto.Service.TRANSFER,
        ContextDto.ServicePrimitive.CONFIRM, false);

    final InterfaceDataUnit56Dto iduConfirmationDto = new InterfaceDataUnit56Dto(
        contextResponseDto, confirmMessageContent);

    Optional<String> result = Optional.empty();
    try {
      String idu = objectMapper.writeValueAsString(iduConfirmationDto);
      result = Optional.of(idu);
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    return result;
  }
}
