/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SymKeyGenSpec.
 * @brief @~english Implementation of the class SymKeyGenSpec.
 */

package ca.griis.security.api.domain.spec.generator.symm;


import ca.griis.security.api.domain.spec.SecuritySpec;
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
 * @brief @~french Définit la specification de génération de clé symétrique.
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
public abstract class SymKeyGenSpec extends SecuritySpec {
  protected final Integer keyBitLength;

  protected SymKeyGenSpec(String algo, Integer keyBitLength) {
    super(algo);

    this.keyBitLength = keyBitLength;
  }

  public Integer getKeyBitLength() {
    return keyBitLength;
  }

  @Override
  public Map<String, String> getParameters() {
    return Map.of("keyBitLength", String.valueOf(keyBitLength));
  }
}
