/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe StampVerifier.
 * @brief @~english Implementation of the StampVerifier class.
 */

package ca.griis.speds.transport.service.security;

import ca.griis.cryptography.hash.entity.Hash;
import ca.griis.cryptography.hash.hashing.Sha512Hashing;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.SilentIgnoredException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;

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
 * @brief @~french Vérie des sceaux d'un message transport.
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
 *      2025-08-08 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class StampVerifier {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(StampVerifier.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie les sceaux d'un message transport.
   * @param pdu Message de la couche transport.
   * @param ha512Hashing Fonction de hachage.
   * @exception SilentIgnoredException En cas qu'un PDU ou le sceau est invalide.
   *
   * @par Tâches
   *      S.O.
   */
  public void verifyStamps(ProtocolDataUnit4TraDto pdu, Sha512Hashing hashing) {
    logger.trace(Trace.ENTER_METHOD_2, "pdu", pdu, "hashing", hashing);

    // Vérification de l'intégrité de l'entête et du contenu
    final String sduHeader;
    try {
      sduHeader = SharedObjectMapper.getInstance().getMapper().writeValueAsString(pdu.getHeader());
    } catch (JsonProcessingException e) {
      throw new SilentIgnoredException(e.getMessage());
    }

    // Hasher l'entête et le contenu
    final Hash headerEncrypt = hashing.hash(sduHeader.getBytes(StandardCharsets.UTF_8));
    final Hash contentEncrypt =
        hashing.hash(pdu.getContent().getBytes(StandardCharsets.UTF_8));
    final String sealHeaderSduGenerated = headerEncrypt.asBase64();
    final String sealContentSduGenerated = contentEncrypt.asBase64();

    // Vérifier l'intégrité de l'entête et du contenu
    if (!pdu.getStamp().getHeaderSeal().equals(sealHeaderSduGenerated)) {
      throw new SilentIgnoredException("Header seal are not the same");
    }

    if (!pdu.getStamp().getContentSeal().equals(sealContentSduGenerated)) {
      throw new SilentIgnoredException("Content seal are not the same");
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }
}
