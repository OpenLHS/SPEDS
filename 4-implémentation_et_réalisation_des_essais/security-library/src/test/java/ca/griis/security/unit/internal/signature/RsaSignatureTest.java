package ca.griis.security.unit.internal.signature;

import static org.junit.jupiter.api.Assertions.*;

import ca.griis.security.api.domain.DigitalSignature;
import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.domain.spec.csprng.EfficientCsprngSpec;
import ca.griis.security.api.domain.spec.sign.EfficientRsaSignatureFnSpec;
import ca.griis.security.internal.asymmetric.generator.Ed25519KeysGenerator;
import ca.griis.security.internal.asymmetric.generator.RsaKeysGenerator;
import ca.griis.security.internal.signature.signing.RsaSigning;
import ca.griis.security.internal.signature.signing.Signing;
import ca.griis.security.internal.signature.verification.RsaVerifySigning;
import ca.griis.security.internal.signature.verification.VerifySigning;
import java.security.KeyPair;
import org.junit.jupiter.api.Test;

public class RsaSignatureTest {
  private static final byte[] msg =
      new byte[] {
          (byte) 0xe0,
          0x4f,
          (byte) 0xd0,
          0x20,
          (byte) 0xea,
          0x3a,
          0x69,
          0x10,
          (byte) 0xa2,
          (byte) 0xd8,
          0x08,
          0x00,
          0x2b,
          0x30,
          0x30,
          (byte) 0x9d
      };

  @Test
  public void givenPairedSignatureThenMatchingSignature() {
    // given
    CsprngSpec csprngSpec = new EfficientCsprngSpec();
    EfficientRsaSignatureFnSpec signatureFnSpec = new EfficientRsaSignatureFnSpec();
    KeyPair keyPair = new RsaKeysGenerator(2048).generateKeyPair(csprngSpec);
    Signing signing = new RsaSigning(keyPair.getPrivate(), signatureFnSpec.getParameterSpec());
    VerifySigning verifySigning =
        new RsaVerifySigning(keyPair.getPublic(), signatureFnSpec.getParameterSpec());

    // when
    DigitalSignature digitalSignature = signing.sign(msg, csprngSpec);
    Boolean verifiedMsg = verifySigning.verify(msg, digitalSignature);

    // then
    assertTrue(verifiedMsg, "Signature doesn't match.");
  }

  @Test
  public void testSignWithInvalidKey() {
    // given
    CsprngSpec csprngSpec = new EfficientCsprngSpec();
    EfficientRsaSignatureFnSpec signatureFnSpec = new EfficientRsaSignatureFnSpec();
    KeyPair keyPair = new Ed25519KeysGenerator().generateKeyPair(csprngSpec);
    Signing signing = new RsaSigning(keyPair.getPrivate(), signatureFnSpec.getParameterSpec());

    // then
    assertThrows(SecurityException.class, () -> signing.sign(msg, csprngSpec));
  }

  @Test
  public void testVerifySignWithInvalidKey() {
    // given
    CsprngSpec csprngSpec = new EfficientCsprngSpec();
    EfficientRsaSignatureFnSpec signatureFnSpec = new EfficientRsaSignatureFnSpec();
    KeyPair keyPair = new RsaKeysGenerator(2048).generateKeyPair(csprngSpec);
    Signing signing = new RsaSigning(keyPair.getPrivate(), signatureFnSpec.getParameterSpec());

    KeyPair keyPair2 = new Ed25519KeysGenerator().generateKeyPair(csprngSpec);
    VerifySigning verifySigning =
        new RsaVerifySigning(keyPair2.getPublic(), signatureFnSpec.getParameterSpec());

    // when
    DigitalSignature digitalSignature = signing.sign(msg, csprngSpec);
    Boolean verifiedMsg = verifySigning.verify(msg, digitalSignature);

    // then
    assertFalse(verifiedMsg, "Signature doesn't match.");
  }
}
