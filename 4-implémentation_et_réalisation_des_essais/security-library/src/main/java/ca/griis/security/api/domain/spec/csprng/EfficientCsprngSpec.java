/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe EfficientCsprngSpec.
 * @brief @~english Implementation of the class EfficientCsprngSpec.
 */

package ca.griis.security.api.domain.spec.csprng;

import java.nio.charset.StandardCharsets;
import java.security.DrbgParameters;
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
 * @brief @~french Définit la specification du générateur aléatoire efficient.
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
public class EfficientCsprngSpec extends CsprngSpec {
  public EfficientCsprngSpec() {
    super(DrbgParameters.instantiation(
        128, DrbgParameters.Capability.PR_AND_RESEED,
        "StrongSecureRandom-v1".getBytes(StandardCharsets.UTF_8)));
  }

  @Override
  public Map<String, String> getParameters() {
    return Map.of(
        "strength", "128",
        "capability", DrbgParameters.Capability.PR_AND_RESEED.toString(),
        "personalizationString", "StrongSecureRandom-v1");
  }
}
