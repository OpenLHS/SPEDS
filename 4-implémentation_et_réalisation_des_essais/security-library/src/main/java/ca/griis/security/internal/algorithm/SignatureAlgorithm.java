/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'énumération SignatureAlgorithm.
 * @brief @~english Implements the SignatureAlgorithm enumerated type.
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
 * @brief @~french Énumère les algorithmes pris en charge par la JDK pour la signature numérique.
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
 * @note 2020-06-17 [CB] - Standard Algorithm Name Documentation for JDK 8 (voir
 *       https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html)
 *
 * @par Tâches
 *      S.O.
 */

public enum SignatureAlgorithm {
  /** @~french algorithme de hachage SHA3-512 avec algorithme de signature RSA (RSASSA-PSS) */
  /** @~english SHA-256 hash algorithm with RSA signature algorithm */
  RSA("RSASSA-PSS"),

  /** @~french aalgorithme de signature Ed25519 */
  /** @~english Ed25519 signature algorithm */
  ed25519("Ed25519");

  private final String algorithm;

  SignatureAlgorithm(String algorithm) {
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
