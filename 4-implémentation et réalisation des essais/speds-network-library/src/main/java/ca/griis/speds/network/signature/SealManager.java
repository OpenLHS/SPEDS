/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SealManager.
 * @brief @~english SealManager class implementation.
 */

package ca.griis.speds.network.signature;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.cryptography.asymmetric.signature.entity.DigitalSignature;
import ca.griis.cryptography.asymmetric.signature.signing.RsaSigning;
import ca.griis.cryptography.asymmetric.signature.verification.RsaVerifySigning;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.service.exception.InvalidSignatureException;
import ca.griis.speds.network.service.exception.SerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
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
 * @brief @~french Permet de gérer des estampilles au sein des messages Réseau.
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
 *      2025-03-03 [CB] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class SealManager {
  private static final GriisLogger logger = getLogger(SealManager.class);

  private static final String CERTIFICATE_ALGORITHM = "X.509";

  private final ObjectMapper objectMapper;

  public SealManager(ObjectMapper objectMapper) {
    logger.trace(Trace.ENTER_METHOD_1, "objectMapper", objectMapper);
    this.objectMapper = objectMapper;
  }

  /**
   * @brief @~english «Description of the function»
   * @param value «Parameter description»
   * @param seal «Parameter description»
   * @param privateKey «Parameter description»
   * @exception SerializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Crée un sceau à partir d'une valeur donnée.
   * @param value La valeur servant à la création du sceau.
   * @param seal Le type de sceau créé.
   * @param privateKey la clé privée utilisée pour créer le sceau.
   * @exception SerializationException Erreur survenue lors de la création du sceau.
   * @return Le sceau créé à partir de la valeur.
   *
   * @par Tâches
   *      S.O.
   */
  public String createSeal(Object value, Seal seal, PrivateKey privateKey) {
    logger.trace(Trace.ENTER_METHOD_3, "value", value, "seal", seal, "privateKey", privateKey);

    final String result;
    try {
      final String message =
          value instanceof String ? (String) value : objectMapper.writeValueAsString(value);
      result = Base64.getEncoder()
          .encodeToString(new RsaSigning((RSAPrivateKey) privateKey).sign(message.getBytes(
              StandardCharsets.UTF_8)).getBytes());
    } catch (JsonProcessingException | SecurityException e) {
      final String exception =
          "Cannot serialize " + seal.name() + " seal: " + e.getMessage();
      logger.error(exception);
      throw new SerializationException(exception);
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param value «Parameter description»
   * @param seal «Parameter description»
   * @param certificatePem «Parameter description»
   * @param signature «Parameter description»
   * @exception InvalidSignatureException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie l'intégrité d'un objet en validant sa signature associée à l'aide d'un
   *        certificat cryptographique.
   * @param value La valeur dont on veut vérifier l'intégrité.
   * @param seal Le type de sceau fourni.
   * @param certificatePem le certificat associé à la clé privée ayant servi à la création de la
   *        signature.
   * @param signature la signature fournie pour valider l'intégrité de la valeur en Base64.
   * @exception InvalidSignatureException Erreur qui survient lorsqu'il est impossible de vérifier
   *            l'intégrité de la valeur.
   * @return Valeur booléenne vraie si la valeur est intègre, et fausse sinon.
   *
   * @par Tâches
   *      S.O.
   */
  public Boolean verifySeal(Object value, Seal seal, String certificatePem,
      String signature) {
    logger.trace(Trace.ENTER_METHOD_4, "value", value, "seal", seal, "certificatePem",
        certificatePem, "signature", signature);

    Boolean result;
    try {
      final CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_ALGORITHM);
      final Certificate certificate =
          cf.generateCertificate(
              new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));

      final byte[] message =
          value instanceof String ? ((String) value).getBytes(StandardCharsets.UTF_8)
              : objectMapper.writeValueAsString(value).getBytes(StandardCharsets.UTF_8);
      result = new RsaVerifySigning((RSAPublicKey) certificate.getPublicKey()).verify(message,
          new DigitalSignature(
              Base64.getDecoder().decode(signature.getBytes(StandardCharsets.UTF_8))));
    } catch (SecurityException | JsonProcessingException | CertificateException e) {
      final String exception =
          "Cannot verify " + seal.name() + " seal: " + e.getMessage();
      logger.error(exception);
      result = Boolean.FALSE;
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
