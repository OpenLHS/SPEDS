/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SealVerifier.
 * @brief @~english Contains description of SealVerifier class.
 */

package ca.griis.speds.session.internal.service.seal;

import ca.griis.cryptography.asymmetric.signature.entity.DigitalSignature;
import ca.griis.cryptography.asymmetric.signature.verification.RsaVerifySigning;
import ca.griis.cryptography.encryption.Decryptor;
import ca.griis.cryptography.hash.entity.Hash;
import ca.griis.cryptography.hash.hashing.Hashing;
import ca.griis.cryptography.hash.hashing.Sha512Hashing;
import ca.griis.cryptography.symmetric.encryption.AesGcmDecryptor;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.exception.CipherException;
import ca.griis.speds.session.internal.util.KeyAlgorithm;
import ca.griis.speds.session.internal.util.KeyMapping;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.BufferUnderflowException;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKey;

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
 * @brief @~french Permet de gérer des estampilles au sein des messages session.
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
 *      2025-03-17 [SSC] - Implémentation initiale<br>
 *      2025-06-29 [MD] - Split SealManager && parameter ObjectMapper<br>
 *
 * @par Tâches
 *      S.O.
 */
public class SealVerifier {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(SealVerifier.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérification du sceau signé
   * @param value l'object à partir duquel on compare la signature
   * @param publicKey la clé publique
   * @param seal le sceau à vérifier
   * @exception CipherException Le seau n'a pu être vérifié
   * @return Vrai si le sceau est vérifié avec succès
   */
  public Boolean verifySeal(ObjectMapper sharedMapper, Object value, String publicKey,
      String seal) {
    logger.trace(Trace.ENTER_METHOD_3, "value", value, "publicKey", publicKey, "seal", seal);

    Boolean result = false;
    try {
      final Hash stamp = createStampHash(value, sharedMapper);

      final RsaVerifySigning verifySigning =
          new RsaVerifySigning(
              (RSAPublicKey) KeyMapping.getPublicKeyFromString(publicKey, KeyAlgorithm.RSA));
      final DigitalSignature ds =
          new DigitalSignature(Base64.getDecoder().decode(seal.getBytes(StandardCharsets.UTF_8)));
      result = verifySigning.verify(stamp.getBytes(), ds);
    } catch (CipherException | SecurityException | JsonProcessingException
        | IllegalArgumentException e) {
      logger.debug("Failed to verify seal: {}", e.toString());
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérification du sceau chiffré avec un algorithme de chiffrement symétrique
   * @param value L'object à partir duquel on compare
   * @param secretKey la clé secrète skak
   * @param seal le sceau à vérifier
   * @param sharedMapper l'objet de serialisation
   * @return Vrai si le sceau est vérifié avec succès
   */
  public Boolean verifySymmetricalSeal(Object value, SecretKey secretKey, String seal,
      ObjectMapper sharedMapper) {
    logger.trace(Trace.ENTER_METHOD_4, "value", value, "secretKey", secretKey, "seal", seal,
        "sharedMapper", sharedMapper);
    Boolean result = false;
    try {
      Hash stamp = createStampHash(value, sharedMapper);

      Decryptor decryptor = new AesGcmDecryptor(secretKey);
      byte[] valueByte = Base64.getDecoder().decode(seal);
      byte[] decryptedContent = decryptor.decrypt(valueByte);
      result = Arrays.equals(decryptedContent, stamp.getBytes());
    } catch (BufferUnderflowException | SecurityException | JsonProcessingException
        | IllegalArgumentException e) {
      logger.debug("Failed to verify seal: {}", e.toString());
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Création de l'empreinte numérique
   * @param value L'object à hasher
   * @param sharedMapper l'objet de serialisation
   * @exception JsonProcessingException Erreur de sérialisation
   * @return le hash de l'empreinte
   */
  private Hash createStampHash(Object value, ObjectMapper sharedMapper)
      throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "value", value);
    // création de l'empreinte numérique
    byte[] bytes = sharedMapper.writeValueAsBytes(value);
    Hashing sha512 = new Sha512Hashing();
    final Hash stamp = sha512.hash(bytes);

    logger.trace(Trace.EXIT_METHOD_1, "stamp", stamp);
    return stamp;
  }
}
