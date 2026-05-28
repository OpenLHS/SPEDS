package ca.griis.security.spec.env;

import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.security.api.domain.spec.certificate.CertificateVerifSpec;
import ca.griis.security.api.domain.spec.cipher.asym.StrongRsaCipherSpec;
import ca.griis.security.api.domain.spec.cipher.symm.AesCipherSpec;
import ca.griis.security.api.domain.spec.csprng.StrongCsprngSpec;
import ca.griis.security.api.domain.spec.dh.StrongX25519KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.generator.asym.Ed25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.StrongRsaKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.X25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.Aes256KeyGenSpec;
import ca.griis.security.api.domain.spec.hash.Sha512Spec;
import ca.griis.security.api.domain.spec.sign.Ed25519SignatureFnSpec;
import ca.griis.security.api.domain.spec.sign.StrongRsaSignatureFnSpec;
import java.util.Map;

public class StrongProfileSpecs {
  public static Map<String, SecuritySpec> createSpecs() {
    return Map.ofEntries(
        Map.entry("CSPRNG", new StrongCsprngSpec()),
        Map.entry("Hashing", new Sha512Spec()),
        Map.entry("AES-Gen", new Aes256KeyGenSpec()),
        Map.entry("AES-GCM", new AesCipherSpec()),
        Map.entry("RSA-Encipherment-Signature-Gen", new StrongRsaKeyPairGenSpec()),
        Map.entry("25519-Signature-Gen", new Ed25519KeyPairGenSpec()),
        Map.entry("25519-DH-Gen", new X25519KeyPairGenSpec()),
        Map.entry("RSA-Encipherment", new StrongRsaCipherSpec()),
        Map.entry("25519-DH", new StrongX25519KeyAgreementFnSpec()),
        Map.entry("RSA-Signature", new StrongRsaSignatureFnSpec()),
        Map.entry("25519-Signature", new Ed25519SignatureFnSpec()),
        Map.entry("PKIX", new CertificateVerifSpec()));
  }
}
