/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe StrongRsaCipherSpec.
 * @brief @~english Implementation of the class StrongRsaCipherSpec.
 */

package ca.griis.security.api.domain.spec.cipher.asym;

import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;

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
 * @brief @~french Définit la specification de chiffrement RSA fort.
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
public abstract class RsaCipherSpec extends AsymCipherSpec {
  private final AlgorithmParameterSpec parameterSpec;

  public RsaCipherSpec(String algo, OAEPParameterSpec parameterSpec) {
    super(algo);

    this.parameterSpec = parameterSpec;
  }

  public AlgorithmParameterSpec getParameterSpec() {
    return parameterSpec;
  }
}
