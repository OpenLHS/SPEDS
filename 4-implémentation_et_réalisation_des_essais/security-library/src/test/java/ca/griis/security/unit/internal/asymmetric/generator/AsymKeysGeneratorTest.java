package ca.griis.security.unit.internal.asymmetric.generator;

import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.domain.spec.csprng.EfficientCsprngSpec;
import ca.griis.security.api.domain.spec.generator.asym.EfficientRsaKeyPairGenSpec;
import ca.griis.security.internal.algorithm.KeyPairGeneratorAlgorithm;
import ca.griis.security.internal.asymmetric.generator.AsymKeysGenerator;
import org.junit.jupiter.api.Test;

public class AsymKeysGeneratorTest {
  private AsymKeysGenerator asymKeysGenerator;

  @Test
  public void testGenerateKeyPairWithInvalidAlgoSpec() {
    // Given
    CsprngSpec csprngSpec = new EfficientCsprngSpec();
    EfficientRsaKeyPairGenSpec asymKeyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    asymKeysGenerator = new AsymKeysGenerator(KeyPairGeneratorAlgorithm.X25519,
        asymKeyPairGenSpec.getKeyGenParameterSpec()) {};

    // Then
    assertThrows(SecurityException.class, () -> asymKeysGenerator.generateKeyPair(csprngSpec));
  }
}
