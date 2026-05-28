package ca.griis.security.unit.api.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.security.api.domain.Digest;
import ca.griis.security.api.domain.DigitalSignature;
import ca.griis.security.api.domain.SecurityProfile;
import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.security.api.domain.spec.cipher.asym.AsymCipherSpec;
import ca.griis.security.api.domain.spec.cipher.asym.EfficientRsaCipherSpec;
import ca.griis.security.api.domain.spec.cipher.asym.StrongRsaCipherSpec;
import ca.griis.security.api.domain.spec.cipher.symm.AesCipherSpec;
import ca.griis.security.api.domain.spec.cipher.symm.SymCipherSpec;
import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.domain.spec.csprng.EfficientCsprngSpec;
import ca.griis.security.api.domain.spec.csprng.StrongCsprngSpec;
import ca.griis.security.api.domain.spec.dh.EfficientX25519KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.dh.KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.dh.StrongX25519KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.generator.asym.AsymKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.Ed25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.EfficientRsaKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.StrongRsaKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.X25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.Aes128KeyGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.Aes256KeyGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.SymKeyGenSpec;
import ca.griis.security.api.domain.spec.hash.HashFnSpec;
import ca.griis.security.api.domain.spec.hash.Sha256Spec;
import ca.griis.security.api.domain.spec.hash.Sha512Spec;
import ca.griis.security.api.domain.spec.sign.Ed25519SignatureFnSpec;
import ca.griis.security.api.domain.spec.sign.EfficientRsaSignatureFnSpec;
import ca.griis.security.api.domain.spec.sign.SignatureFnSpec;
import ca.griis.security.api.exception.DecryptException;
import ca.griis.security.api.service.DefaultSecurityService;
import ca.griis.security.internal.algorithm.CipherAlgorithm;
import ca.griis.security.internal.algorithm.KeyPairGeneratorAlgorithm;
import ca.griis.security.internal.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.security.internal.algorithm.SignatureAlgorithm;
import ca.griis.security.unit.util.cipher.CipherHelper;
import ca.griis.security.unit.util.generator.asymm.KeyPairGeneratorHelper;
import ca.griis.security.unit.util.generator.symm.SecretKeyGeneratorHelper;
import ca.griis.security.unit.util.hash.HashingHelper;
import ca.griis.security.unit.util.signature.DigitalSignatureHelper;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.spec.NamedParameterSpec;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

public class DefaultSecurityServiceTest {
  private final DefaultSecurityService service = new DefaultSecurityService();

  @Test
  public void testProfilSecuritySpecsSize() {
    Map<String, SecuritySpec> securityProfileSpec =
        service.getProfilSecuritySpecs(SecurityProfile.Efficient);

    assertFalse(securityProfileSpec.isEmpty());
  }

  @Test
  public void testGenerateDigestWithSha256() {
    // Créer un HashFnSpec concret
    HashFnSpec hashFnSpec = new Sha256Spec();

    // Données à hasher
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    Digest actualDigest = service.generateDigest(hashFnSpec, data);

    assertNotNull(actualDigest);
  }

  @Test
  public void testGenerateDigestWithSha512() {
    // Créer un HashFnSpec concret
    HashFnSpec hashFnSpec = new Sha512Spec();

    // Données à hasher
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    Digest actualDigest = service.generateDigest(hashFnSpec, data);

    assertNotNull(actualDigest);
  }

  @Test
  public void testGenerateDigestIllegalArgument() {
    // Créer un HashFnSpec concret
    HashFnSpec hashFnSpec = createHashFnSpec("illegalArgument");


    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    assertThrows(IllegalArgumentException.class, () -> {
      service.generateDigest(hashFnSpec, data);
    });
  }

  @Test
  public void testGenerateVerifyDigestWithSha256() {
    // HashFnSpec SHA-256
    HashFnSpec hashFnSpec = new Sha256Spec();

    // Données à hasher
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    // Hash
    HashingHelper hashingHelper = new HashingHelper();
    Digest digest = hashingHelper.hash(data, hashFnSpec.getAlgo());

    Boolean result = service.verifyDigest(hashFnSpec, digest, data);

    assertNotNull(result);
    assertTrue(result);
  }

  @Test
  public void testGenerateVerifyDigestWithSha512() {
    // HashFnSpec SHA-512
    HashFnSpec hashFnSpec = new Sha512Spec();

    // Données à hasher
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    // Hash
    HashingHelper hashingHelper = new HashingHelper();
    Digest digest = hashingHelper.hash(data, hashFnSpec.getAlgo());

    Boolean result = service.verifyDigest(hashFnSpec, digest, data);

    assertNotNull(result);
    assertTrue(result);
  }

  @Test
  public void testGenerateVerifyDigestWithWrongHashingAlgo() {
    // HashFnSpec à mauvais algo
    HashFnSpec hashFnSpec = createHashFnSpec("WrongAlgo");

    // Données à hasher
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    // Hash
    HashingHelper hashingHelper = new HashingHelper();

    Digest digest = hashingHelper.hash(data, "SHA-256");

    assertThrows(IllegalArgumentException.class, () -> {
      service.verifyDigest(hashFnSpec, digest, data);
    });
  }

  @Test
  public void testGenerateSecretKey256Efficient() {
    CsprngSpec givingCsprng = new EfficientCsprngSpec();
    SymKeyGenSpec givingSymKeyGen = new Aes256KeyGenSpec();

    SecretKey secretKey = service.generateSecretKey(givingCsprng, givingSymKeyGen);

    assertNotNull(secretKey);
    assertEquals(32, secretKey.getEncoded().length);
  }

  @Test
  public void testGenerateSecretKey128Efficient() {
    CsprngSpec givingCsprng = new EfficientCsprngSpec();
    SymKeyGenSpec givingSymKeyGen = new Aes128KeyGenSpec();

    SecretKey secretKey = service.generateSecretKey(givingCsprng, givingSymKeyGen);

    assertNotNull(secretKey);
    assertEquals(16, secretKey.getEncoded().length);
  }

  @Test
  public void testGenerateSecretKey256Strong() {
    CsprngSpec givingCsprng = new StrongCsprngSpec();
    SymKeyGenSpec givingSymKeyGen = new Aes256KeyGenSpec();

    SecretKey secretKey = service.generateSecretKey(givingCsprng, givingSymKeyGen);

    assertNotNull(secretKey);
    assertEquals(32, secretKey.getEncoded().length);
  }

  @Test
  public void testGenerateSecretKey128Strong() {
    CsprngSpec givingCsprng = new StrongCsprngSpec();
    SymKeyGenSpec givingSymKeyGen = new Aes128KeyGenSpec();

    SecretKey secretKey = service.generateSecretKey(givingCsprng, givingSymKeyGen);

    assertNotNull(secretKey);
    assertEquals(16, secretKey.getEncoded().length);
  }

  @Test
  public void testGenerateSecretKeyWrongAlgo() {
    CsprngSpec givingCsprng = new StrongCsprngSpec();
    SymKeyGenSpec givingSymKeyGen = new SymKeyGenSpec("INVALID_ALGO", -1) {
      @Override
      public Map<String, String> getParameters() {
        return Map.of("keyBitLength", "128");
      }
    };

    assertThrows(SecurityException.class, () -> {
      service.generateSecretKey(givingCsprng, givingSymKeyGen);
    });
  }

  @Test
  public void testSymEncrypt() {
    SecretKeyGeneratorHelper secretKeyGeneratorHelper = new SecretKeyGeneratorHelper();
    CsprngSpec csprng = new StrongCsprngSpec();
    SymCipherSpec symCipherSpec = new AesCipherSpec();
    SymKeyGenSpec symKeyGen = new Aes128KeyGenSpec();
    SecretKey secretKey = secretKeyGeneratorHelper
        .generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, symKeyGen.getKeyBitLength());
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    byte[] encryptedData =
        service.symEncrypt(csprng, symCipherSpec, secretKey, data);

    assertNotNull(encryptedData);
    assertEquals(37, encryptedData.length);
  }

  @Test
  public void testIllegalArgumentExceptionSymEncrypt() {
    SecretKeyGeneratorHelper secretKeyGeneratorHelper = new SecretKeyGeneratorHelper();
    CsprngSpec csprng = new StrongCsprngSpec();
    SymCipherSpec symCipherSpec = new SymCipherSpec("invalidAlgo") {
      @Override
      public Map<String, String> getParameters() {
        return Map.of("algorithm", "invalidAlgo");
      }
    };
    SymKeyGenSpec symKeyGen = new Aes128KeyGenSpec();
    SecretKey secretKey = secretKeyGeneratorHelper
        .generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, symKeyGen.getKeyBitLength());
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    assertThrows(IllegalArgumentException.class, () -> {
      service.symEncrypt(csprng, symCipherSpec, secretKey, data);
    });
  }

  @Test
  public void testSymDecrypt() {
    CipherHelper cipherHelper = new CipherHelper();

    CsprngSpec csprng = new StrongCsprngSpec();
    SymCipherSpec symCipherSpec = new AesCipherSpec();
    SymKeyGenSpec symKeyGen = new Aes128KeyGenSpec();
    SecretKey secretKey = service.generateSecretKey(csprng, symKeyGen);

    byte[] originalData = "Test data".getBytes(StandardCharsets.UTF_8);
    byte[] encryptedData =
        cipherHelper.encryptSymm(originalData, secretKey);
    byte[] decryptedData =
        service.symDecrypt(csprng, symCipherSpec, secretKey, encryptedData);

    assertNotNull(decryptedData);
    assertArrayEquals(originalData, decryptedData);
    assertEquals("Test data", new String(decryptedData, StandardCharsets.UTF_8));
  }

  @Test
  public void testIllegalArgumentExceptionSymDecrypt() {
    CsprngSpec csprng = new StrongCsprngSpec();
    SymCipherSpec symCipherSpec = new SymCipherSpec("invalidAlgo") {
      @Override
      public Map<String, String> getParameters() {
        return Map.of("algorithm", "invalidAlgo");
      }
    };
    SymKeyGenSpec symKeyGen = new Aes128KeyGenSpec();
    SecretKey secretKey = service.generateSecretKey(csprng, symKeyGen);
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    assertThrows(IllegalArgumentException.class, () -> {
      service.symDecrypt(csprng, symCipherSpec, secretKey, data);
    });
  }

  @Test
  public void testSymDecryptException() {
    SecretKeyGeneratorHelper secretKeyGeneratorHelper = new SecretKeyGeneratorHelper();
    CsprngSpec csprng = new StrongCsprngSpec();
    SymCipherSpec symCipherSpec = new AesCipherSpec();
    SymKeyGenSpec symKeyGen = new Aes128KeyGenSpec();
    SecretKey secretKey = secretKeyGeneratorHelper
        .generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, symKeyGen.getKeyBitLength());
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    assertThrows(DecryptException.class, () -> {
      service.symDecrypt(csprng, symCipherSpec, secretKey, data);
    });
  }

  @Test
  public void testGenerateRsaKeyPair() {
    // given
    CsprngSpec strongCsprngSpec = new StrongCsprngSpec();

    AsymKeyPairGenSpec strongRsaKeyPairGenSpec = new StrongRsaKeyPairGenSpec();

    // when
    KeyPair keyPair =
        service.generateKeyPair(strongCsprngSpec, strongRsaKeyPairGenSpec);

    // then
    assertNotNull(keyPair);
  }

  @Test
  public void testGenerateEd25519KeyPair() {
    // given
    CsprngSpec strongCsprngSpec = new StrongCsprngSpec();

    AsymKeyPairGenSpec ed25519KeyPairGenSpec = new Ed25519KeyPairGenSpec();

    // when
    KeyPair keyPair =
        service.generateKeyPair(strongCsprngSpec, ed25519KeyPairGenSpec);

    // then
    assertNotNull(keyPair);
  }

  @Test
  public void testGenerateX25519KeyPair() {
    // given
    CsprngSpec strongCsprngSpec = new StrongCsprngSpec();

    AsymKeyPairGenSpec keyPairGenSpec = new X25519KeyPairGenSpec();

    // when
    KeyPair keyPair = service.generateKeyPair(strongCsprngSpec, keyPairGenSpec);

    // then
    assertNotNull(keyPair);
  }

  @Test
  public void testGenerateKeyPairWithWrongAlgo() {
    // given
    CsprngSpec strongCsprngSpec = new StrongCsprngSpec();

    AsymKeyPairGenSpec keyPairGenSpec = createKeyPairGenSpec("WrongAlgo");

    // when
    assertThrows(IllegalArgumentException.class,
        () -> service.generateKeyPair(strongCsprngSpec, keyPairGenSpec));
  }

  @Test
  public void testStrongRsaAsymEncrypt() {
    // given
    CsprngSpec strongCsprngSpec = new StrongCsprngSpec();
    AsymCipherSpec asymCipherSpec = new StrongRsaCipherSpec();

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    StrongRsaKeyPairGenSpec keyPairGenSpec = new StrongRsaKeyPairGenSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    // when
    byte[] encryptedData = service.asymEncrypt(strongCsprngSpec, asymCipherSpec,
        keyPair.getPublic(), data);

    // then
    assertNotNull(encryptedData);
  }

  @Test
  public void testEfficientRsaAsymEncrypt() {
    // given
    CsprngSpec csprngSpec = new EfficientCsprngSpec();
    AsymCipherSpec asymCipherSpec = new EfficientRsaCipherSpec();

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec keyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    // when
    byte[] encryptedData =
        service.asymEncrypt(csprngSpec, asymCipherSpec, keyPair.getPublic(), data);

    // then
    assertNotNull(encryptedData);
  }

  @Test
  public void testAsymEncryptWithWrongAlgo() {
    // given
    CsprngSpec csprngSpec = new EfficientCsprngSpec();
    AsymCipherSpec asymCipherSpec = createAsymCipherSpec("WrongAlgo");

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec keyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    // then
    assertThrows(IllegalArgumentException.class, () -> service
        .asymEncrypt(csprngSpec, asymCipherSpec, keyPair.getPublic(), data));
  }

  @Test
  public void testStrongRsaDecrypt() {
    // given
    CsprngSpec csprngSpec = new StrongCsprngSpec();

    StrongRsaCipherSpec asymCipherSpec = new StrongRsaCipherSpec();

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    StrongRsaKeyPairGenSpec keyPairGenSpec = new StrongRsaKeyPairGenSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    CipherHelper cipherHelper = new CipherHelper();
    byte[] encryptedData = cipherHelper.encrypt(data, CipherAlgorithm.RSA, keyPair.getPublic(),
        asymCipherSpec.getParameterSpec());

    // when
    byte[] actualDecryptedData = service.asymDecrypt(csprngSpec, asymCipherSpec,
        keyPair.getPrivate(), encryptedData);

    // then
    assertNotNull(actualDecryptedData);
    assertEquals(new String(actualDecryptedData, StandardCharsets.UTF_8),
        new String(data, StandardCharsets.UTF_8));
  }

  @Test
  public void testEfficientRsaDecrypt() {
    // given
    CsprngSpec csprngSpec = new EfficientCsprngSpec();

    EfficientRsaCipherSpec asymCipherSpec = new EfficientRsaCipherSpec();

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec keyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    CipherHelper cipherHelper = new CipherHelper();
    byte[] encryptedData = cipherHelper.encrypt(data, CipherAlgorithm.RSA, keyPair.getPublic(),
        asymCipherSpec.getParameterSpec());

    // when
    byte[] actualDecryptedData = service.asymDecrypt(csprngSpec, asymCipherSpec,
        keyPair.getPrivate(), encryptedData);

    // then
    assertNotNull(actualDecryptedData);
    assertEquals(new String(actualDecryptedData, StandardCharsets.UTF_8),
        new String(data, StandardCharsets.UTF_8));
  }

  @Test
  public void testAsymDecryptWithWrongAlgo() {
    CsprngSpec csprngSpec = new EfficientCsprngSpec();

    AsymCipherSpec asymCipherSpec = createAsymCipherSpec("WrongAlgo");

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec keyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    EfficientRsaCipherSpec efficientAsymCipherSpec = new EfficientRsaCipherSpec();
    CipherHelper cipherHelper = new CipherHelper();
    byte[] encryptedData = cipherHelper.encrypt(data, CipherAlgorithm.RSA, keyPair.getPublic(),
        efficientAsymCipherSpec.getParameterSpec());

    assertThrows(IllegalArgumentException.class, () -> service
        .asymDecrypt(csprngSpec, asymCipherSpec, keyPair.getPrivate(), encryptedData));
  }

  @Test
  public void testStrongX25519GenerateSharedSecretKey() {
    // given
    CsprngSpec csprngSpec = new StrongCsprngSpec();

    StrongX25519KeyAgreementFnSpec keyAgreementFnSpec = new StrongX25519KeyAgreementFnSpec();

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    X25519KeyPairGenSpec keyPairGenSpec = new X25519KeyPairGenSpec();
    KeyPair originatorKeyPair =
        keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.X25519,
            keyPairGenSpec.getNamedParameterSpec());

    KeyPair recipientKeyPair =
        keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.X25519,
            keyPairGenSpec.getNamedParameterSpec());

    // when
    SecretKey secretKey = service.generateSharedSecretKey(csprngSpec,
        keyAgreementFnSpec, originatorKeyPair, recipientKeyPair.getPublic());

    // then
    assertNotNull(secretKey);
  }


  @Test
  public void testEfficientX25519GenerateSharedSecretKey() {
    CsprngSpec csprngSpec = new EfficientCsprngSpec();

    EfficientX25519KeyAgreementFnSpec keyAgreementFnSpec = new EfficientX25519KeyAgreementFnSpec();

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    X25519KeyPairGenSpec keyPairGenSpec = new X25519KeyPairGenSpec();
    KeyPair originatorKeyPair =
        keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.X25519,
            keyPairGenSpec.getNamedParameterSpec());

    KeyPair recipientKeyPair =
        keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.X25519,
            keyPairGenSpec.getNamedParameterSpec());

    // when
    SecretKey secretKey = service.generateSharedSecretKey(csprngSpec,
        keyAgreementFnSpec, originatorKeyPair, recipientKeyPair.getPublic());

    // then
    assertNotNull(secretKey);
  }

  @Test
  public void testX25519GenerateSharedSecretKeyWithWrongAlgo() {
    // given
    CsprngSpec csprngSpec = new EfficientCsprngSpec();

    KeyAgreementFnSpec keyAgreementFnSpec = createKeyAgreementSpec("WrongAlgo");

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    X25519KeyPairGenSpec keyPairGenSpec = new X25519KeyPairGenSpec();
    KeyPair originatorKeyPair =
        keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.X25519,
            keyPairGenSpec.getNamedParameterSpec());

    KeyPair recipientKeyPair =
        keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.X25519,
            keyPairGenSpec.getNamedParameterSpec());

    // then
    assertThrows(IllegalArgumentException.class,
        () -> service.generateSharedSecretKey(csprngSpec, keyAgreementFnSpec,
            originatorKeyPair, recipientKeyPair.getPublic()));
  }

  @Test
  public void testGenerateSignature() {
    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec keyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    CsprngSpec csprngSpec = new EfficientCsprngSpec();
    SignatureFnSpec fnSpec = new EfficientRsaSignatureFnSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    DigitalSignature actualDigitalSignature =
        service.generateSignature(csprngSpec, fnSpec, keyPair.getPrivate(), data);

    assertNotNull(actualDigitalSignature);
  }

  @Test
  public void testGenerateSignatureWithEd() {
    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    CsprngSpec csprngSpec = new EfficientCsprngSpec();
    SignatureFnSpec fnSpec = new Ed25519SignatureFnSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.ed25519,
        NamedParameterSpec.ED25519);
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    DigitalSignature actualDigitalSignature =
        service.generateSignature(csprngSpec, fnSpec, keyPair.getPrivate(), data);

    assertNotNull(actualDigitalSignature);
  }

  @Test
  public void testGenerateSignatureIllegalArgument() {
    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec keyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    CsprngSpec csprngSpec = new EfficientCsprngSpec();
    SignatureFnSpec fnSpec = new SignatureFnSpec("INVALID_ALGO") {
      @Override
      public Map<String, String> getParameters() {
        return Map.of("algorithm", "INVALID_ALGO");
      }
    };
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());
    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    assertThrows(IllegalArgumentException.class, () -> service
        .generateSignature(csprngSpec, fnSpec, keyPair.getPrivate(), data));
  }

  @Test
  public void testVerifySignature() {
    EfficientRsaSignatureFnSpec efficientRsaSignatureFnSpec = new EfficientRsaSignatureFnSpec();

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec keyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());
    DigitalSignatureHelper digitalSignatureHelper =
        new DigitalSignatureHelper(SignatureAlgorithm.RSA, keyPair.getPrivate(),
            efficientRsaSignatureFnSpec.getParameterSpec());
    CsprngSpec csprngSpec = new EfficientCsprngSpec();

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);
    DigitalSignature someDigitalSignature = digitalSignatureHelper.sign(data, csprngSpec);

    Boolean actualVerify = service.verifySignature(efficientRsaSignatureFnSpec,
        keyPair.getPublic(), data,
        someDigitalSignature);
    assertTrue(actualVerify);
  }

  @Test
  public void testVerifySignatureEd() {
    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.ed25519,
        NamedParameterSpec.ED25519);

    DigitalSignatureHelper digitalSignatureHelper =
        new DigitalSignatureHelper(SignatureAlgorithm.ed25519, keyPair.getPrivate(), null);
    CsprngSpec csprngSpec = new EfficientCsprngSpec();
    SignatureFnSpec fnSpec = new Ed25519SignatureFnSpec();

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);

    DigitalSignature someDigitalSignature = digitalSignatureHelper.sign(data, csprngSpec);

    Boolean actualVerify = service
        .verifySignature(fnSpec, keyPair.getPublic(), data, someDigitalSignature);

    assertTrue(actualVerify);
  }

  @Test
  public void testVerifySignatureIllegalArgument() {
    EfficientRsaSignatureFnSpec efficientRsaSignatureFnSpec = new EfficientRsaSignatureFnSpec();
    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec keyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    KeyPair keyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());
    DigitalSignatureHelper digitalSignatureHelper =
        new DigitalSignatureHelper(SignatureAlgorithm.RSA, keyPair.getPrivate(),
            efficientRsaSignatureFnSpec.getParameterSpec());
    CsprngSpec csprngSpec = new EfficientCsprngSpec();

    SignatureFnSpec fnSpec = new SignatureFnSpec("INVALID_ALGO") {
      @Override
      public Map<String, String> getParameters() {
        return Map.of("algorithm", "INVALID_ALGO");
      }
    };

    byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);
    DigitalSignature someDigitalSignature = digitalSignatureHelper.sign(data, csprngSpec);

    assertThrows(IllegalArgumentException.class, () -> service
        .verifySignature(fnSpec, keyPair.getPublic(), data, someDigitalSignature));
  }

  @Test
  public void testX25519GenerateSharedSecretKeyWithNonX25519Keys() {
    // given
    CsprngSpec csprngSpec = new EfficientCsprngSpec();

    KeyAgreementFnSpec keyAgreementFnSpec = new EfficientX25519KeyAgreementFnSpec();

    KeyPairGeneratorHelper keyPairGeneratorHelper = new KeyPairGeneratorHelper();
    EfficientRsaKeyPairGenSpec keyPairGenSpec = new EfficientRsaKeyPairGenSpec();
    KeyPair originatorKeyPair =
        keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
            keyPairGenSpec.getKeyGenParameterSpec());

    KeyPair recipientKeyPair = keyPairGeneratorHelper.generateKeyPair(KeyPairGeneratorAlgorithm.RSA,
        keyPairGenSpec.getKeyGenParameterSpec());

    // then
    assertThrows(RuntimeException.class,
        () -> service.generateSharedSecretKey(csprngSpec, keyAgreementFnSpec,
            originatorKeyPair, recipientKeyPair.getPublic()));
  }

  private HashFnSpec createHashFnSpec(String algo) {
    return new HashFnSpec(algo) {
      @Override
      public Map<String, String> getParameters() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mdName", algo);
        params.put("provider", "SUN");
        return params;
      }
    };
  }

  private AsymKeyPairGenSpec createKeyPairGenSpec(String algo) {
    return new AsymKeyPairGenSpec(algo) {
      @Override
      public Map<String, String> getParameters() {
        return null;
      }
    };
  }

  private AsymCipherSpec createAsymCipherSpec(String algo) {
    return new AsymCipherSpec(algo) {
      @Override
      public Map<String, String> getParameters() {
        return null;
      }
    };
  }

  private KeyAgreementFnSpec createKeyAgreementSpec(String algo) {
    return new KeyAgreementFnSpec(algo) {
      @Override
      public Map<String, String> getParameters() {
        return null;
      }
    };
  }
}
