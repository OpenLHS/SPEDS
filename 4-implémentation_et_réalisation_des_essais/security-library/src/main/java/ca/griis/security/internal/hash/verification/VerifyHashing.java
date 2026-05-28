/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe VerifyHashing.
 * @brief @~english Implementation of the VerifyHashing class.
 */

package ca.griis.security.internal.hash.verification;

import ca.griis.security.api.domain.Digest;
import ca.griis.security.internal.hash.hashing.Hashing;
import java.security.MessageDigest;

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
 * @brief @~french Vérification qu'une empreinte numérique correspond à une entrée à l'aide d'une
 *        fonction de hachage.
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
public abstract class VerifyHashing {
  private final Hashing hashing;

  protected VerifyHashing(Hashing hashing) {
    this.hashing = hashing;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie si une empreinte numérique correspond à une entrée.
   * @param bytes une liste d'octets
   * @param hash l'empreinte numérique
   * @return la valeur <code>true</code> si l'empreinte numérique correspond à une entrée, la valeur
   *         <code>false</code> sinon
   * @par Tâches
   *      S.O.
   */
  public Boolean verify(byte[] bytes, Digest hash) {
    Boolean equals = MessageDigest.isEqual(hash.bytes(), hashing.hash(bytes).bytes());
    return equals;
  }
}
