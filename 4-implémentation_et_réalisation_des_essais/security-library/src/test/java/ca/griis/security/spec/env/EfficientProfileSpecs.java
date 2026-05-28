package ca.griis.security.spec.env;

import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.security.api.domain.spec.certificate.CertificateVerifSpec;
import ca.griis.security.api.domain.spec.cipher.asym.EfficientRsaCipherSpec;
import ca.griis.security.api.domain.spec.cipher.symm.AesCipherSpec;
import ca.griis.security.api.domain.spec.csprng.EfficientCsprngSpec;
import ca.griis.security.api.domain.spec.dh.EfficientX25519KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.generator.asym.Ed25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.EfficientRsaKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.X25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.Aes128KeyGenSpec;
import ca.griis.security.api.domain.spec.hash.Sha256Spec;
import ca.griis.security.api.domain.spec.sign.Ed25519SignatureFnSpec;
import ca.griis.security.api.domain.spec.sign.EfficientRsaSignatureFnSpec;
import java.util.Map;

public class EfficientProfileSpecs {
  public static Map<String, SecuritySpec> createSpecs() {
    return Map.ofEntries(
        Map.entry("CSPRNG", new EfficientCsprngSpec()),
        Map.entry("Hashing", new Sha256Spec()),
        Map.entry("AES-Gen", new Aes128KeyGenSpec()),
        Map.entry("AES-GCM", new AesCipherSpec()),
        Map.entry("RSA-Encipherment-Signature-Gen", new EfficientRsaKeyPairGenSpec()),
        Map.entry("25519-Signature-Gen", new Ed25519KeyPairGenSpec()),
        Map.entry("25519-DH-Gen", new X25519KeyPairGenSpec()),
        Map.entry("RSA-Encipherment", new EfficientRsaCipherSpec()),
        Map.entry("25519-DH", new EfficientX25519KeyAgreementFnSpec()),
        Map.entry("RSA-Signature", new EfficientRsaSignatureFnSpec()),
        Map.entry("25519-Signature", new Ed25519SignatureFnSpec()),
        Map.entry("PKIX", new CertificateVerifSpec()));
  }
}
