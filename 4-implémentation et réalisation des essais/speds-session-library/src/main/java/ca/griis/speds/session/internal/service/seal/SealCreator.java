/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SealCreator.
 * @brief @~english Contains description of SealCreator class.
 */

package ca.griis.speds.session.internal.service.seal;

import ca.griis.cryptography.asymmetric.signature.entity.DigitalSignature;
import ca.griis.cryptography.asymmetric.signature.signing.RsaSigning;
import ca.griis.cryptography.encryption.Encryptor;
import ca.griis.cryptography.hash.entity.Hash;
import ca.griis.cryptography.hash.hashing.Hashing;
import ca.griis.cryptography.hash.hashing.Sha512Hashing;
import ca.griis.cryptography.symmetric.encryption.AesGcmEncryptor;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.exception.CipherException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
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
public class SealCreator {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(SealCreator.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Création d'un sceau: une signature avec un algorithme de signature
   * @param value L'object à partir duquel le sceau est créé
   * @param privateKey la clé privée
   * @param sharedMapper l'objet de serialisation
   * @exception CipherException Erreur dans la création du sceau
   * @return la signature du sceau
   */
  public String createSeal(Object value, PrivateKey privateKey, ObjectMapper sharedMapper) {
    logger.trace(Trace.ENTER_METHOD_2, "value", value, "privateKey", privateKey);

    final Hash stamp;
    try {
      stamp = createStampHash(value, sharedMapper);
    } catch (JsonProcessingException e) {
      throw new CipherException(e.getMessage());
    }
    // Signature
    final RsaSigning rsaSign = new RsaSigning((RSAPrivateKey) privateKey);
    final DigitalSignature ds = rsaSign.sign(stamp.getBytes());
    final String signedSeal = Base64.getEncoder().encodeToString(ds.getBytes());

    logger.trace(Trace.EXIT_METHOD_1, "signedSeal", signedSeal);
    return signedSeal;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Création d'un sceau à partir de l'algorithme de chiffrement symétrique
   * @param value L'object à partir duquel le sceau est créé
   * @param skak la clé secrète
   * @param sharedMapper l'objet de serialisation
   * @exception CipherException Erreur dans la création du sceau
   * @return le sceau chiffré
   */
  public String createSymmetricalSeal(Object value, SecretKey skak, ObjectMapper sharedMapper) {
    logger.trace(Trace.ENTER_METHOD_2, "value", value, "skak", skak);
    Hash stamp = null;
    try {
      stamp = createStampHash(value, sharedMapper);
    } catch (JsonProcessingException e) {
      throw new CipherException(e.getMessage());
    }

    Encryptor encryptor = new AesGcmEncryptor(skak);
    byte[] encryptedSealB = encryptor.encrypt(stamp.getBytes());
    String encryptedSeal = Base64.getEncoder().encodeToString(encryptedSealB);

    logger.trace(Trace.EXIT_METHOD_1, "encryptedSeal", encryptedSeal);
    return encryptedSeal;
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
