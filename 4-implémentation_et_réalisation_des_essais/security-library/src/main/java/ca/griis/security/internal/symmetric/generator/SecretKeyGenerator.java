/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SecretKeyGenerator.
 * @brief @~english
 */

package ca.griis.security.internal.symmetric.generator;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.internal.random.RandomProvider;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
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
 * @brief @~french Générateur de clés symétriques.
 * @par Détails
 *      -# Classe qui permet de faire une façade sur la couche sécurité de Java.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-12-17 [BD] - Implémentation initiale.<br>
 *
 * @par Tâches
 */
public class SecretKeyGenerator {
  private static final GriisLogger logger = getLogger(SecretKeyGenerator.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Génère une clé symétrique.
   * @param algorithm le type d'algorithme à utiliser
   * @param keyBitLength la longueur de la clé à générer
   * @return une clé symétrique
   *
   * @par Tâches
   *      S.O.
   */
  public static SecretKey generateSymmetricKey(String algorithm,
      Integer keyBitLength, CsprngSpec csprngSpec) {
    logger.trace(Trace.ENTER_METHOD_2, "algorithm", algorithm, "keyBitLength", keyBitLength);

    SecureRandom secureRandom = RandomProvider.getSecureRandom(csprngSpec);

    SecretKey secretKey;
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
      keyGen.init(keyBitLength, secureRandom);
      secretKey = keyGen.generateKey();
    } catch (NoSuchAlgorithmException | InvalidParameterException e) {
      throw new SecurityException("Unable to generate a secretKey", e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "secretKey", System.identityHashCode(secretKey));
    return secretKey;
  }
}
