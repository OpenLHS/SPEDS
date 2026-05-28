package ca.griis.security.unit.internal.asymmetric.encryption;

import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.security.api.domain.spec.cipher.asym.EfficientRsaCipherSpec;
import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.domain.spec.csprng.EfficientCsprngSpec;
import ca.griis.security.api.domain.spec.generator.asym.EfficientRsaKeyPairGenSpec;
import ca.griis.security.api.exception.DecryptException;
import ca.griis.security.internal.algorithm.CipherAlgorithm;
import ca.griis.security.internal.algorithm.KeyPairGeneratorAlgorithm;
import ca.griis.security.internal.asymmetric.encryption.RsaDecryptor;
import ca.griis.security.internal.asymmetric.encryption.RsaEncryptor;
import ca.griis.security.unit.util.cipher.CipherHelper;
import ca.griis.security.unit.util.generator.asymm.KeyPairGeneratorHelper;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.Test;

public class AsymCipherTest {
  @Test
  public void testEncryptWithInvalidAlgoSpec() {
    // Given
    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec rsaKeyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        rsaKeyPairGenSpec.getKeyGenParameterSpec());

    RsaEncryptor encryptor = new RsaEncryptor((RSAPublicKey) keyPair.getPublic(),
        rsaKeyPairGenSpec.getKeyGenParameterSpec());

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);
    CsprngSpec csprngSpec = new EfficientCsprngSpec();

    // Then
    assertThrows(SecurityException.class, () -> encryptor.encrypt(data, csprngSpec));
  }

  @Test
  public void testDecryptWithInvalidAlgoSpec() {
    // Given
    EfficientRsaCipherSpec rsaCipherSpec = new EfficientRsaCipherSpec();

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec rsaKeyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        rsaKeyPairGenSpec.getKeyGenParameterSpec());

    CipherHelper cipherHelper = new CipherHelper();

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);
    CsprngSpec csprngSpec = new EfficientCsprngSpec();

    byte[] encryptedData = cipherHelper.encrypt(data, CipherAlgorithm.RSA, keyPair.getPublic(),
        rsaCipherSpec.getParameterSpec());

    RsaDecryptor decryptor = new RsaDecryptor((RSAPrivateKey) keyPair.getPrivate(),
        rsaKeyPairGenSpec.getKeyGenParameterSpec());

    // Then
    assertThrows(DecryptException.class, () -> decryptor.decrypt(encryptedData, csprngSpec));
  }
}
