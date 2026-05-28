/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe StrongRsaSignatureFnSpec.
 * @brief @~english Implementation of the class StrongRsaSignatureFnSpec.
 */

package ca.griis.security.api.domain.spec.sign;

import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
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
 * @brief @~french Définit la specification de fonction de signature RSA fort.
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
public class StrongRsaSignatureFnSpec extends RsaSignatureFnSpec {
  public StrongRsaSignatureFnSpec() {
    super(new PSSParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, 64, 1));
  }

  @Override
  public Map<String, String> getParameters() {
    return Map.of(
        "mdName", "SHA-512",
        "mgfName", "MGF1",
        "mgfSpec", "SHA-512",
        "saltLen", "64",
        "trailerField", "1");
  }
}
