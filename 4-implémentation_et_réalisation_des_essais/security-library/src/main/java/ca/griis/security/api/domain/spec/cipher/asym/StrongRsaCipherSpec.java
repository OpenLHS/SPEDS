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

import java.security.spec.MGF1ParameterSpec;
import java.util.Map;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

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
public class StrongRsaCipherSpec extends RsaCipherSpec {
  public StrongRsaCipherSpec() {
    super(
        "RSA/ECB/OAEPWithSHA-256AndMGF1Padding",
        new OAEPParameterSpec(
            "SHA-512",
            "MGF1",
            MGF1ParameterSpec.SHA512,
            PSource.PSpecified.DEFAULT));
  }

  @Override
  public Map<String, String> getParameters() {
    return Map.of(
        "mdName", "SHA-512",
        "mgfName", "MGF1",
        "mgfSpec", "SHA-512",
        "pSrc", "");
  }
}
