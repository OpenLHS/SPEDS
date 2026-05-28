/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe AesCipherSpec.
 * @brief @~english Implementation of the class AesCipherSpec.
 */

package ca.griis.security.api.domain.spec.cipher.symm;

import java.util.Map;

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
 * @brief @~french Définit la specification de chiffrement AES.
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
public class AesCipherSpec extends SymCipherSpec {
  private final Integer gcmTagBitSize;
  private final Integer gcmIvBitLength;

  public AesCipherSpec() {
    super("AES/GCM/NoPadding");

    this.gcmTagBitSize = 128;
    this.gcmIvBitLength = 96;
  }

  public Integer getGcmTagBitSize() {
    return gcmTagBitSize;
  }

  public Integer getGcmIvLength() {
    return gcmIvBitLength;
  }

  @Override
  public Map<String, String> getParameters() {
    return Map.of(
        "gcmTagBitSize", String.valueOf(gcmTagBitSize),
        "gcmIvBitLength", String.valueOf(gcmIvBitLength));
  }
}
