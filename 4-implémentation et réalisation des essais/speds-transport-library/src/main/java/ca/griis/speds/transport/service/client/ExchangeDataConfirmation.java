/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ExchangeDataConfirmation.
 * @brief @~english Implementation of the ExchangeDataConfirmation class.
 */

package ca.griis.speds.transport.service.client;

import ca.griis.cryptography.hash.hashing.Sha512Hashing;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.SilentIgnoredException;
import ca.griis.speds.transport.service.security.StampVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;

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
 * @brief @~french Un processus de confirmation d'échange de données.
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
 *      2025-03-04 [JM] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ExchangeDataConfirmation {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(ExchangeDataConfirmation.class);

  private final Set<String> pendingMessagesId;
  private final StampVerifier stampVerfier;

  public ExchangeDataConfirmation(Set<String> pendingMessagesId) {
    logger.trace(Trace.ENTER_METHOD_1, " pendingMessagesId", pendingMessagesId);

    this.pendingMessagesId = pendingMessagesId;
    this.stampVerfier = new StampVerifier();
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Traite la réception d'un message réponse de la couche transport.
   * @param iduDto IDU de la couche inférieure.
   * @exception SilentIgnoredException Exception silencieuse.
   *
   * @par Tâches
   *      S.O.
   */
  public void confirm(InterfaceDataUnit45Dto iduDto) {
    logger.trace(Trace.ENTER_METHOD_1, "iduDto", iduDto);

    final ProtocolDataUnit4TraDto pdu;
    try {
      pdu = SharedObjectMapper.getInstance().getMapper().readValue(iduDto.getMessage(),
          ProtocolDataUnit4TraDto.class);
    } catch (JsonProcessingException e) {
      throw new SilentIgnoredException(e.getMessage());
    }

    final Sha512Hashing hashing = new Sha512Hashing();
    stampVerfier.verifyStamps(pdu, hashing);

    pendingMessagesId.remove(pdu.getHeader().getId());
  }
}
