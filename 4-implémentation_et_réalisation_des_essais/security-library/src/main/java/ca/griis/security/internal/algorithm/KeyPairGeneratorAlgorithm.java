/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'énumération KeyPairGeneratorAlgorithm.
 * @brief @~english Implements the KeyPairGeneratorAlgorithm enumerated type.
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
 * @brief @~french Énumère les algorithmes pris en charge par la JDK pour la génération de clés
 *        publiques pour le chiffrement/déchiffrement asymétrique ou la signature numérique.
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
 * @note 2020-06-17 [CB] - Standard Algorithm Name Documentation for JDK 17 (voir
 *       https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html)
 *
 * @par Tâches
 *      S.O.
 */
public enum KeyPairGeneratorAlgorithm {
  /** @~french algorithme de chiffrement RSA */
  /** @~english RSA encryption algorithm */
  RSA("RSA"),
  /** @~french algorithme de signature avec la courbe elliptique 25519 */
  /** @~english elliptic Curve 25519 signature algorithm */
  ed25519("Ed25519"),
  /** @~french algorithme d'accord de clé Diffie-Hellman avec la courbe elliptique 25519 */
  /** @~english elliptic Curve 25519 signature algorithm */
  X25519("X25519");

  private final String algorithm;

  KeyPairGeneratorAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  /**
   * @brief @~english «Description of the method»
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
