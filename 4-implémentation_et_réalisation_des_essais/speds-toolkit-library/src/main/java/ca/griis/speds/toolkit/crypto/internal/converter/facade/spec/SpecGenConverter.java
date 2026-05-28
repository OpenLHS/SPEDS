/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpecGenConverter.
 * @brief @~english Contains description of SpecGenConverter class.
 */

package ca.griis.speds.toolkit.crypto.internal.converter.facade.spec;

import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.security.api.domain.spec.cipher.asym.StrongRsaCipherSpec;
import ca.griis.security.api.domain.spec.csprng.StrongCsprngSpec;
import ca.griis.security.api.domain.spec.generator.asym.Ed25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.EfficientRsaKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.X25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.Aes128KeyGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.Aes256KeyGenSpec;
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
 * @brief @~french Convertit une catégorie d'algorithme de SPEDS à une spécification à un générateur
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
public class SpecGenConverter implements Function<String, SecuritySpec> {
  private static final Map<String, SecuritySpec> mapping = Map.ofEntries(
      Map.entry("CSPRNG", new StrongCsprngSpec()),
      Map.entry("AES128-GCM", new Aes128KeyGenSpec()),
      Map.entry("AES256-GCM", new Aes256KeyGenSpec()),
      Map.entry("RSA4096-OAEP-MGF1-SHA256", new EfficientRsaKeyPairGenSpec()),
      Map.entry("RSA4096-OAEP-MGF1-SHA512", new StrongRsaCipherSpec()),
      Map.entry("RSA4096-PSS-MGF1-SHA256", new EfficientRsaSignatureFnSpec()),
      Map.entry("RSA4096-PSS-MGF1-SHA512", new StrongRsaSignatureFnSpec()),
      Map.entry("X25519", new X25519KeyPairGenSpec()),
      Map.entry("Ed25519", new Ed25519KeyPairGenSpec()));

  @Override
  public SecuritySpec apply(String algo) {
    return mapping.get(algo);
  }
}
