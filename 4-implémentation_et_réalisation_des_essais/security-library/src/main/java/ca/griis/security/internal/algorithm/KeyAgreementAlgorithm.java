/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'énumération KeyAgreementAlgorithm.
 * @brief @~english Implements the KeyAgreementAlgorithm enumerated type.
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
 * @brief @~french Énumère les algorithmes pris en charge par la JDK pour les accords de clés.
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
 *      2025-12-17 [BD] - Implémentation initiale.
 *
 * @par Tâches
 *      S.O.
 */
public enum KeyAgreementAlgorithm {
  /** @~french algorithme d'accord de clé Diffie-Hellman avec la courbe elliptique 25519 */
  /** @~english elliptic Curve25519 Diffie-Hellman key agreement algorithm */
  X25519;

  /**
   * @brief @~english «Description of the method»
   * @return «Return description»
   *
   * @brief @~french Récupère l'algorithme correspondant au type énuméré dans le format standard
   *        pris en charge par la JDK 8.
   * @return une chaine de caractères qui contient un nom d'algorithme
   */
  public String getAlgorithm() {
    return name();
  }
}
