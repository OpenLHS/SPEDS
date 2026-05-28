/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpecProvider.
 * @brief @~english Implementation of the class SpecProvider.
 */

package ca.griis.security.internal.spec;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.security.api.domain.SecurityProfile;
import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.security.api.domain.spec.certificate.CertificateVerifSpec;
import ca.griis.security.api.domain.spec.cipher.asym.EfficientRsaCipherSpec;
import ca.griis.security.api.domain.spec.cipher.asym.StrongRsaCipherSpec;
import ca.griis.security.api.domain.spec.cipher.symm.AesCipherSpec;
import ca.griis.security.api.domain.spec.csprng.EfficientCsprngSpec;
import ca.griis.security.api.domain.spec.csprng.StrongCsprngSpec;
import ca.griis.security.api.domain.spec.dh.EfficientX25519KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.dh.StrongX25519KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.generator.asym.Ed25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.EfficientRsaKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.StrongRsaKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.X25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.Aes128KeyGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.Aes256KeyGenSpec;
import ca.griis.security.api.domain.spec.hash.Sha256Spec;
import ca.griis.security.api.domain.spec.hash.Sha512Spec;
import ca.griis.security.api.domain.spec.sign.Ed25519SignatureFnSpec;
import ca.griis.security.api.domain.spec.sign.EfficientRsaSignatureFnSpec;
import ca.griis.security.api.domain.spec.sign.StrongRsaSignatureFnSpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details «Detailed description of the component (optional)»
 * @par Model «Model (Abstract, automation, etc.) (optional)»
 * @par Conception «Conception description (criteria and constraints) (optional)»
 * @par Limits «Limits description (optional)»
 *
 * @brief @~french Fournisseur des spécifications de sécurité des profils.
 * @par Détails
 *      Cette classe définit le fournisseur des spécifications de sécurité des différents profils.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 * @par Historique
 *      2025-12-17 [BD] - Création de la classe.
 * @par Tâches
 *      S.O.
 */
public class SpecProvider {
  private static final GriisLogger logger = getLogger(SpecProvider.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Obtient les spécifications de sécurité d'un profil.
   * @param securityProfile profil de sécurité
   * @return la signature
   * @par Tâches S.O.
   */
  public Map<String, SecuritySpec> getProfilSecuritySpecs(SecurityProfile securityProfile) {
    logger.trace(Trace.ENTER_METHOD_1, "securityProfile", securityProfile);

    Map<String, SecuritySpec> specs = new HashMap<>();
    if (securityProfile.equals(SecurityProfile.Strongest)) {

      specs.put("CSPRNG", new StrongCsprngSpec());

      specs.put("Hashing", new Sha512Spec());

      specs.put("AES-Gen", new Aes256KeyGenSpec());

      specs.put("RSA-Encipherment-Signature-Gen", new StrongRsaKeyPairGenSpec());
      specs.put("RSA-Encipherment", new StrongRsaCipherSpec());
      specs.put("RSA-Signature", new StrongRsaSignatureFnSpec());

      specs.put("25519-DH", new StrongX25519KeyAgreementFnSpec());
    } else {

      specs.put("CSPRNG", new EfficientCsprngSpec());

      specs.put("Hashing", new Sha256Spec());

      specs.put("AES-Gen", new Aes128KeyGenSpec());

      specs.put("RSA-Encipherment-Signature-Gen", new EfficientRsaKeyPairGenSpec());
      specs.put("RSA-Encipherment", new EfficientRsaCipherSpec());
      specs.put("RSA-Signature", new EfficientRsaSignatureFnSpec());

      specs.put("25519-DH", new EfficientX25519KeyAgreementFnSpec());
    }

    specs.put("AES-GCM", new AesCipherSpec());

    specs.put("25519-Signature-Gen", new Ed25519KeyPairGenSpec());
    specs.put("25519-DH-Gen", new X25519KeyPairGenSpec());
    specs.put("25519-Signature", new Ed25519SignatureFnSpec());

    specs.put("PKIX", new CertificateVerifSpec());

    logger.trace(Trace.EXIT_METHOD_1, "specs", specs);
    return specs;
  }
}
