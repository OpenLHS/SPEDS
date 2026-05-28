/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'énumération CipherAlgorithm.
 * @brief @~english Implements the CipherAlgorithm enumerated type.
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
 * @brief @~french Énumère les algorithmes pris en charge par la JDK pour le
 *        chiffrement/déchiffrement.
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
public enum CipherAlgorithm {
  /** @~french algorithme de chiffrement RSA */
  /** @~english RSA encryption algorithm */
  RSA("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"),
  /** @~french algorithme de chiffrement AES avec le mode d'opération GCM */
  /** @~english AES encryption algorithm with GCM mode of operation */
  AESGCM("AES/GCM/NoPadding");

  private final String algorithm;

  CipherAlgorithm(String algorithm) {
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
