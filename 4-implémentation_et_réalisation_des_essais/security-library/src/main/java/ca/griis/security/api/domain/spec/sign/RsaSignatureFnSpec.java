/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe RsaSignatureFnSpec.
 * @brief @~english Implementation of the class RsaSignatureFnSpec.
 */

package ca.griis.security.api.domain.spec.sign;

import java.security.spec.PSSParameterSpec;

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
 * @brief @~french Définit la specification de fonction de signature RSA.
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
public abstract class RsaSignatureFnSpec extends SignatureFnSpec {
  private final PSSParameterSpec parameterSpec;

  public RsaSignatureFnSpec(PSSParameterSpec parameterSpec) {
    super("RSASSA-PSS");

    this.parameterSpec = parameterSpec;
  }

  public PSSParameterSpec getParameterSpec() {
    return parameterSpec;
  }
}
