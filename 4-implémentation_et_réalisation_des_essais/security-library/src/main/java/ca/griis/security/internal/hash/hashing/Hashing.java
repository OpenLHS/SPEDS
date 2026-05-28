/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe Hashing.
 * @brief @~english Implementation of the Hashing class.
 */

package ca.griis.security.internal.hash.hashing;

import ca.griis.security.api.domain.Digest;
import ca.griis.security.internal.algorithm.MessageDigestAlgorithm;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
 * @brief @~french Génération d'une empreinte numérique à l'aide d'une fonction de hachage.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-12-17 [BD] - Implémentation initiale.
 *
 * @par Tâches
 *      S.O.
 */
public abstract class Hashing {
  private final MessageDigestAlgorithm algorithm;

  protected Hashing(MessageDigestAlgorithm algorithm) {
    this.algorithm = algorithm;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Génère une empreinte numérique à l'aide d'une fonction de hachage.
   * @param bytes une liste d'octets
   * @return l'empreinte numérique
   * @par Tâches
   *      S.O.
   */
  public Digest hash(byte[] bytes) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance(algorithm.getAlgorithm());
    } catch (NoSuchAlgorithmException e) {
      throw new SecurityException(e);
    }

    Digest digest = new Digest(messageDigest.digest(bytes));
    return digest;
  }
}
