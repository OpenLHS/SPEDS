/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe EfficientRsaKeyPairGenSpec.
 * @brief @~english Implementation of the class EfficientRsaKeyPairGenSpec.
 */

package ca.griis.security.api.domain.spec.generator.asym;

import java.security.spec.RSAKeyGenParameterSpec;
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
 * @brief @~french Définit la specification de génération de paire de clé RSA efficient.
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
public class EfficientRsaKeyPairGenSpec extends RsaKeyPairGenSpec {
  public EfficientRsaKeyPairGenSpec() {
    super(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
  }

  @Override
  public Map<String, String> getParameters() {
    return Map.of("keysize", "2048", "publicExponent", "65537");
  }
}
