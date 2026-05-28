/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe NotifyDispatcher.
 * @brief @~english Implementation of the NotifyDispatcher class.
 */

package ca.griis.speds.link.internal.dispatcher;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.link.api.dto.ContextDto;
import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
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
 *      2026-04-21 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class NotifyDispatcher {
  private static final GriisLogger logger = getLogger(SubmitDispatcher.class);

  private final ObjectMapper objectMapper;

  public NotifyDispatcher(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Optional<String> handle(ProtocolIdu protocolIdu) {
    Optional<String> result = Optional.empty();

    try {
      ContextDto ici = new ContextDto(protocolIdu.destinationUri(), ContextDto.Service.TRANSFER,
          ContextDto.ServicePrimitive.INDICATION, false);
      String sdu = protocolIdu.sdu();
      InterfaceDataUnit56Dto idu = new InterfaceDataUnit56Dto(ici, sdu);

      String iduJson = objectMapper.writeValueAsString(idu);
      result = Optional.of(iduJson);
    } catch (JsonProcessingException ex) {
      logger.error(Error.IGNORED_ERROR, ex);
    }

    return result;
  }
}
