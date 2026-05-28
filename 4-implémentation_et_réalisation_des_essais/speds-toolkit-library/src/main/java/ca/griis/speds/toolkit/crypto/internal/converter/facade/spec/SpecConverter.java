/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpecConverter.
 * @brief @~english Contains description of SpecConverter class.
 */

package ca.griis.speds.toolkit.crypto.internal.converter.facade.spec;

import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.security.api.domain.spec.cipher.asym.EfficientRsaCipherSpec;
import ca.griis.security.api.domain.spec.cipher.asym.StrongRsaCipherSpec;
import ca.griis.security.api.domain.spec.cipher.symm.AesCipherSpec;
import ca.griis.security.api.domain.spec.hash.Sha256Spec;
import ca.griis.security.api.domain.spec.hash.Sha512Spec;
import ca.griis.security.api.domain.spec.sign.Ed25519SignatureFnSpec;
import ca.griis.security.api.domain.spec.sign.EfficientRsaSignatureFnSpec;
import ca.griis.security.api.domain.spec.sign.StrongRsaSignatureFnSpec;
import java.util.Map;
import java.util.function.Function;

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
 * @brief @~french Convertit une catégorie d'algorithme de SPEDS à une spécification
 *        cryptographique.
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
 *      2025-12-19 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class SpecConverter implements Function<String, SecuritySpec> {
  private static final Map<String, SecuritySpec> mapping = Map.ofEntries(
      Map.entry("AES128-GCM", new AesCipherSpec()),
      Map.entry("AES256-GCM", new AesCipherSpec()),
      Map.entry("RSA4096-OAEP-MGF1-SHA256", new EfficientRsaCipherSpec()),
      Map.entry("RSA4096-OAEP-MGF1-SHA512", new StrongRsaCipherSpec()),
      Map.entry("SHA256", new Sha256Spec()),
      Map.entry("SHA512", new Sha512Spec()),
      Map.entry("RSA4096-PSS-MGF1-SHA256", new EfficientRsaSignatureFnSpec()),
      Map.entry("RSA4096-PSS-MGF1-SHA512", new StrongRsaSignatureFnSpec()),
      Map.entry("Ed25519", new Ed25519SignatureFnSpec()));

  @Override
  public SecuritySpec apply(String algo) {
    return mapping.get(algo);
  }
}
