/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe NetworkPduChecker.
 * @brief @~english Contains description of NetworkPduChecker class.
 */

package ca.griis.speds.network.internal.checker;

import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5NETDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.speds.network.internal.security.CertificateChecker;
import ca.griis.speds.network.internal.security.SealManager;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;

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
 * @brief @~french Vérifie la validité d'un message réseau.
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
public final class NetworkPduChecker {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(NetworkPduChecker.class);

  private final SealManager sealManager;
  private final ObjectMapper objectMapper;
  private final CertificateChecker certificateChecker;

  public NetworkPduChecker(CryptographyService service, ObjectMapper objectMapper) {
    this.sealManager = new SealManager(service);
    this.objectMapper = objectMapper;
    this.certificateChecker = new CertificateChecker();
  }

  public PduCheckerResult check(ProtocolDataUnit5NETDto pdu, String entityIri)
      throws JsonProcessingException {
    final var headerDto = pdu.getHeader();
    final var auth = String.valueOf(headerDto.getAuthentification());
    var result = new PduCheckerResult(false, "FAILED: No certificate");

    if (StringUtils.isNotEmpty(auth)) {
      try {
        final var cf = CertificateFactory.getInstance("X.509");
        final var cert = (X509Certificate) cf
            .generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(auth)));

        result = new PduCheckerResult(false,
            "FAILED: The speds-toolkit certificate verification returns false");

        if (certificateChecker.checkCertificate(cert, headerDto.getSourceIri())) {
          final var key = cert.getPublicKey();
          final var header = objectMapper.writeValueAsString(pdu.getHeader());

          if (!sealManager.checkSeal(header, key, pdu.getStamp().getHeaderSeal())) {
            result = new PduCheckerResult(false, "FAILED: Invalid Header Seal");
          } else if (!sealManager.checkSeal(pdu.getContent(), key,
              pdu.getStamp().getContentSeal())) {
            result = new PduCheckerResult(false, "FAILED: Invalid Content Seal");
          } else {
            result = new PduCheckerResult(true,
                "The header and content seal have been successfully validated and are correct");
          }
        }
      } catch (CertificateException e) {
        logger.error(Error.IGNORED_ERROR, e);

        result = new PduCheckerResult(false, "FAILED: Certificate error");
      }
    }

    logger.info(Info.VARIABLE_LOGGING_5,
        "entityIri", entityIri,
        "msgId", pdu.getHeader().getId(),
        "msgType", pdu.getHeader().getMsgtype(),
        "check", result.isValid(),
        "message", result.message());

    return result;
  }
}
