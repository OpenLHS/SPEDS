/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe RsaKeyPairGenSpec.
 * @brief @~english Implementation of the class RsaKeyPairGenSpec.
 */

package ca.griis.security.api.domain.spec.generator.asym;

import java.security.spec.RSAKeyGenParameterSpec;

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
 * @brief @~french Définit la specification de génération de paire de clé RSA.
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
public abstract class RsaKeyPairGenSpec extends AsymKeyPairGenSpec {
  protected final RSAKeyGenParameterSpec keyGenParameterSpec;

  public RsaKeyPairGenSpec(RSAKeyGenParameterSpec keyGenParameterSpec) {
    super("RSA");

    this.keyGenParameterSpec = keyGenParameterSpec;
  }

  public RSAKeyGenParameterSpec getKeyGenParameterSpec() {
    return keyGenParameterSpec;
  }
}
