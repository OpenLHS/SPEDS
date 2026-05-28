/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe HkdfSpec.
 * @brief @~english Implementation of the class HkdfSpec.
 */

package ca.griis.security.api.domain.spec.derivation;

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
 * @brief @~french Définit la specification de l'algorithme de dérivation de clé HkdfSpec.
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
 *      «2026-04-20» [FO] - Implémentation initiale
 * @par Tâches
 *      S.O.
 */
public class HkdfSpec extends SecuritySpec {
  private final String hmac;

  public HkdfSpec(String hmac) {
    super("hkdf");

    this.hmac = hmac;
  }

  @Override
  public Map<String, String> getParameters() {
    return Map.of("algo", algo, "hmac", hmac);
  }
}
