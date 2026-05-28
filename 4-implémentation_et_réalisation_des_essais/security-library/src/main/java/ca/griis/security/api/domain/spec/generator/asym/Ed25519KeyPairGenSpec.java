/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe Ed25519KeyPairGenSpec.
 * @brief @~english Implementation of the class Ed25519KeyPairGenSpec.
 */

package ca.griis.security.api.domain.spec.generator.asym;

import java.security.spec.NamedParameterSpec;
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
 * @brief @~french Définit la specification de génération de paire de clé Ed25519.
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
public class Ed25519KeyPairGenSpec extends AsymKeyPairGenSpec {
  private final NamedParameterSpec namedParameterSpec;

  public Ed25519KeyPairGenSpec() {
    super("Ed25519");

    this.namedParameterSpec = NamedParameterSpec.ED25519;
  }

  public NamedParameterSpec getNamedParameterSpec() {
    return namedParameterSpec;
  }

  @Override
  public Map<String, String> getParameters() {
    return Map.of("stdName", "Ed25519");
  }
}
