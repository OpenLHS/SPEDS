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

package ca.griis.speds.transport.internal.security;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

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
  private final CryptographyService service;

  public StampVerifier(CryptographyService service) {
    this.service = service;
  }

  public Boolean verifyHeader(ProtocolDataUnit4TraDto pdu) {
    var result = false;

    try {
      final byte[] header =
          SharedObjectMapper.getInstance().getMapper().writeValueAsBytes(pdu.getHeader());
      final var hash = service.hash(SpedsConfigItemDto.SpedsLayer.TRANSPORT, header);
      result = verify(hash, pdu.getStamp().getHeaderSeal());
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, new RuntimeException(e));
    }

    return result;
  }

  public Boolean verifyContent(ProtocolDataUnit4TraDto pdu) {
    byte[] contentBytes = pdu.getContent().getBytes(StandardCharsets.UTF_8);
    byte[] hash = service.hash(SpedsConfigItemDto.SpedsLayer.TRANSPORT, contentBytes);
    var result = verify(hash, pdu.getStamp().getContentSeal());

    return result;
  }

  private Boolean verify(byte[] hash, final String seal) {
    var result = false;

    try {
      byte[] sealBytes = Base64.getDecoder().decode(seal);
      result = MessageDigest.isEqual(sealBytes, hash);
    } catch (IllegalArgumentException e) {
      logger.error(Error.IGNORED_ERROR, new RuntimeException(e));
    }

    return result;
  }
}
