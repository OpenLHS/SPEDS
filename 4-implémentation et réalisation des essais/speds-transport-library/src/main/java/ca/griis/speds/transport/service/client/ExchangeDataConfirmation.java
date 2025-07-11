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

import ca.griis.cryptography.hash.entity.Hash;
import ca.griis.cryptography.hash.hashing.Sha512Hashing;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.transport.exception.ContentSealException;
import ca.griis.speds.transport.exception.DeserializationException;
import ca.griis.speds.transport.exception.HeaderSealException;
import ca.griis.speds.transport.exception.SerializationException;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
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
public class ExchangeDataConfirmation {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(ExchangeDataConfirmation.class);

  public static void dataConfirmationProcess(InterfaceDataUnit45Dto iduDto,
      Set<String> sentMessagesId) {
    logger.trace(Trace.ENTER_METHOD_1, "iduDto", iduDto);

    // Vérifier le sceau de l'entête du message
    // Prendre l'entête du message
    final ProtocolDataUnit4TraDto sdu;
    try {
      sdu = SharedObjectMapper.getInstance().getMapper().readValue(iduDto.getMessage(),
          ProtocolDataUnit4TraDto.class);
    } catch (JsonProcessingException e) {
      throw new DeserializationException(e.getMessage());
    }

    if (sentMessagesId.remove(sdu.getHeader().getId().toString())) {
      verifyStamps(sdu);
    }
  }

  private static void verifyStamps(ProtocolDataUnit4TraDto sdu) {
    final String sduHeader;
    try {
      sduHeader =
          SharedObjectMapper.getInstance().getMapper().writeValueAsString(sdu.getHeader());
    } catch (JsonProcessingException e) {
      throw new SerializationException(e.getMessage());
    }

    // Hasher l'entête et le contenu
    final Sha512Hashing sha512Hashing = new Sha512Hashing();
    final Hash headerEncrypt = sha512Hashing.hash(sduHeader.getBytes(StandardCharsets.UTF_8));
    final Hash contentEncrypt =
        sha512Hashing.hash(sdu.getContent().getBytes(StandardCharsets.UTF_8));
    final String sealHeaderSduGenerated = headerEncrypt.asBase64();
    final String sealContentSduGenerated = contentEncrypt.asBase64();

    // Vérifier l'intégrité de l'entête et du contenu
    if (!sdu.getStamp().getHeaderSeal().equals(sealHeaderSduGenerated)) {
      throw new HeaderSealException("Header seal are not the same");
    }

    if (!sdu.getStamp().getContentSeal().equals(sealContentSduGenerated)) {
      throw new ContentSealException("Content seal are not the same");
    }
  }
}
