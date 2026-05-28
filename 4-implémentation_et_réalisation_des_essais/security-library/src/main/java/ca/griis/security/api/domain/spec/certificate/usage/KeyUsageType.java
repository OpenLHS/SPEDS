/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'enumeration KeyUsageType.
 * @brief @~english Implementation of the enumeration KeyUsageType.
 */

package ca.griis.security.api.domain.spec.certificate.usage;

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
 * @brief @~french Enumère les usages de certificat.
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
 *      «2025-12-17» [BD] - Implémentation initiale
 * @par Tâches
 *      S.O.
 */
public enum KeyUsageType {
  DIGITAL_SIGNATURE(0),
  NON_REPUDIATION(1),
  KEY_ENCIPHERMENT(2),
  DATA_ENCIPHERMENT(3),
  KEY_AGREEMENT(4),
  KEY_CERT_SIGN(5),
  CRL_SIGN(6),
  ENCIPHER_ONLY(7),
  DECIPHER_ONLY(8);

  private final int bit;

  KeyUsageType(int bit) {
    this.bit = bit;
  }

  public int getBit() {
    return bit;
  }

  public static KeyUsageType fromBit(int bit) {
    for (KeyUsageType type : KeyUsageType.values()) {
      if (type.getBit() == bit) {
        return type;
      }
    }
    throw new IllegalArgumentException("No bit KeyUsageType : " + bit);
  }
}
