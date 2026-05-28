/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'énumération MessageDigestAlgorithm.
 * @brief @~english Implements the MessageDigestAlgorithm enumerated type.
 */

package ca.griis.security.internal.algorithm;

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
 * @brief @~french Énumère les algorithmes de hachage pris en charge par la JDK pour la création
 *        des empreintes numériques.
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
 *      2025-12-17 [BD] - Implémentation initiale.<br>
 *
 * @par Tâches
 *      S.O.
 */
public enum MessageDigestAlgorithm {
  /** @~french algorithme de hachage SHA-256 */
  /** @~english SHA-256 hash algorithm */
  SHA256("SHA-256"),
  /** @~french algorithme de hachage SHA-512 */
  /** @~english SHA-512 hash algorithm */
  SHA512("SHA-512");

  private final String algorithm;

  MessageDigestAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  /**
   * @brief @~english «Description of the function»
   * @return «Return description»
   *
   * @brief @~french Récupère l'algorithme correspondant au type énuméré dans le format standard
   *        pris en charge par la JDK 8.
   * @return une chaine de caractères qui contient un nom d'algorithme
   */
  public String getAlgorithm() {
    return algorithm;
  }
}
