package ca.griis.speds.toolkit.integration;

import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.computeECDHSharedSecretWithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.decryptAsymmetricWithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.decryptWithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.encryptAsymmetricSha256WithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.encryptAsymmetricSha512WithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.encryptWithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.generateECDHKeyPairWithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.generateSecretKeyFromDHWithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.generateSymmetricKeyWithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.hashSha256WithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.hashSha512WithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.signSha256WithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.signSha512WithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.validateSignSha256WithBouncyCastle;
import static ca.griis.speds.toolkit.integration.utilities.BouncyCastleUtil.validateSignSha512WithBouncyCastle;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.api.exception.NoSuchAlgorithmException;
import ca.griis.speds.toolkit.crypto.api.exception.NoSuchCategoryException;
import ca.griis.speds.toolkit.project.ProjectService;
import ca.griis.speds.toolkit.security.api.CertificateService;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;
import javax.crypto.SecretKey;
import org.apache.jena.iri.IRI;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Cases {

  public static void ctProGe01_01(ProjectService projectService, IRI expected, String projectId,
      String code) throws Exception {
    final IRI actual = assertDoesNotThrow(() -> projectService.getEntityIri(projectId, code));
    assertEquals(expected, actual);
  }

  public static void ctProGe02_01(ProjectService projectService, Boolean expected, String projectId,
      String code, PublicKey publicKey) throws Exception {
    final Boolean actual =
        assertDoesNotThrow(() -> projectService.verifyEntityLegitimacy(projectId, code, publicKey));
    assertEquals(expected, actual);
  }

  public static void ctProGe03_01(ProjectService projectService, PublicKey expected,
      String projectId, String code) throws Exception {
    final PublicKey actual =
        assertDoesNotThrow(() -> projectService.getEntityPublicKey(projectId, code));
    assertEquals(expected, actual);
  }

  public static void ctProGe04_01(ProjectService projectService, String projectId) {
    Boolean actual = projectService.checkProjectActivity(projectId);
    assertTrue(actual);
  }

  public static void ctProGe05_01(ProjectService projectService, String projectId) {
    Boolean actual = projectService.checkPlanActivity(projectId);
    assertTrue(actual);
  }

  public static void ctProSe01_01(CertificateService securityService, Boolean expected,
      X509Certificate certificate, IRI iri) throws Exception {
    final Boolean actual =
        assertDoesNotThrow(() -> securityService.checkCertificateValidity(certificate, iri));
    assertEquals(expected, actual);
  }

  public static void ctCryptSymm_01(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      byte[] data) throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    SecretKey secretKey = cryptographyService.generateSymmetricKey(spedsLayer);
    byte[] encryptData = encryptWithBouncyCastle(secretKey, data);
    byte[] decryptData = cryptographyService.decryptSymmetric(spedsLayer, secretKey, encryptData);
    boolean arrayEqual = Arrays.equals(data, decryptData);
    assertTrue(arrayEqual);
  }

  public static void ctCryptSymm_02(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      byte[] data) throws Exception {
    SecretKey secretKey = generateSymmetricKeyWithBouncyCastle();
    byte[] encryptData = cryptographyService.encryptSymmetric(spedsLayer, secretKey, data);
    byte[] decryptData = decryptWithBouncyCastle(secretKey, encryptData);
    boolean arrayEqual = Arrays.equals(data, decryptData);
    assertTrue(arrayEqual);
  }

  public static void ctCryptAlgo_01(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      AlgorithmCategory algorithmCategory, String expected) {
    String algo = cryptographyService.getAlgorithm(spedsLayer, algorithmCategory);
    assertEquals(expected, algo);
  }

  public static void ctCryptAsym_01(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      KeyPair keyPair, byte[] data) throws Exception {
    byte[] encryptData =
        cryptographyService.encryptAsymmetric(spedsLayer, keyPair.getPublic(), data);
    byte[] decryptData = decryptAsymmetricWithBouncyCastle(keyPair.getPrivate(), encryptData);
    boolean arrayEqual = Arrays.equals(data, decryptData);

    assertTrue(arrayEqual);
  }

  public static void ctCryptAsym_02(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      KeyPair keyPair, byte[] data, String securityProfile) throws Exception {
    byte[] encryptData;
    if (securityProfile.equals("EFFICIENT")) {
      encryptData = encryptAsymmetricSha256WithBouncyCastle(keyPair.getPublic(), data);
    } else if (securityProfile.equals("STRONG")) {
      encryptData = encryptAsymmetricSha512WithBouncyCastle(keyPair.getPublic(), data);
    } else {
      throw new IllegalArgumentException("Unknown security profile: " + securityProfile);
    }

    byte[] decryptData =
        cryptographyService.decryptAsymmetric(spedsLayer, keyPair.getPrivate(), encryptData);

    boolean arrayEqual = Arrays.equals(data, decryptData);

    assertTrue(arrayEqual);
  }

  public static void ctCryptHash_01(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      byte[] data, String securityProfile) throws Exception {
    byte[] toolkitResult = cryptographyService.hash(spedsLayer, data);
    byte[] externResult;
    if (securityProfile.equals("SHA-256")) {
      externResult = hashSha256WithBouncyCastle(data);
    } else if (securityProfile.equals("SHA-512")) {
      externResult = hashSha512WithBouncyCastle(data);
    } else {
      throw new IllegalArgumentException("Unknown security profile: " + securityProfile);
    }

    assertArrayEquals(toolkitResult, externResult);
  }

  public static void ctCryptSign_01(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      KeyPair keyPair, byte[] data, String securityProfile) throws Exception {
    byte[] toolkitSign = cryptographyService.sign(spedsLayer, keyPair.getPrivate(), data);

    boolean validateSign;
    if (securityProfile.equals("EFFICIENT")) {
      validateSign = validateSignSha256WithBouncyCastle(toolkitSign, keyPair.getPublic(), data);
    } else if (securityProfile.equals("STRONG")) {
      validateSign = validateSignSha512WithBouncyCastle(toolkitSign, keyPair.getPublic(), data);
    } else {
      throw new IllegalArgumentException("Unknown security profile: " + securityProfile);
    }

    assertTrue(validateSign);
  }

  public static void ctCryptSign_02(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      KeyPair keyPair, byte[] data, String securityProfile) throws Exception {
    byte[] externSign;
    if (securityProfile.equals("EFFICIENT")) {
      externSign = signSha256WithBouncyCastle(keyPair.getPrivate(), data);
    } else if (securityProfile.equals("STRONG")) {
      externSign = signSha512WithBouncyCastle(keyPair.getPrivate(), data);
    } else {
      throw new IllegalArgumentException("Unknown security profile: " + securityProfile);
    }

    boolean toolkitValidationResult =
        cryptographyService.checkSignatureValidity(spedsLayer, externSign, keyPair.getPublic(),
            data);

    assertTrue(toolkitValidationResult);
  }

  public static void ctCryptDh_01(CryptographyService cryptographyService, SpedsLayer spedsLayer)
      throws Exception {
    KeyPair toolkitKeypair = cryptographyService.chooseDiffieHellmanValue(spedsLayer);

    KeyPair externKeypair = generateECDHKeyPairWithBouncyCastle(toolkitKeypair.getPublic());

    SecretKey toolkitResult = cryptographyService.getDiffieHellmanSecretKey(
        spedsLayer,
        toolkitKeypair,
        externKeypair.getPublic());

    byte[] sharedSecret = computeECDHSharedSecretWithBouncyCastle(
        externKeypair.getPrivate(),
        toolkitKeypair.getPublic());

    int keyBitLength = toolkitResult.getEncoded().length * 8;
    SecretKey externResult = generateSecretKeyFromDHWithBouncyCastle(
        keyBitLength,
        sharedSecret,
        externKeypair.getPublic(),
        toolkitKeypair.getPublic());

    assertArrayEquals(toolkitResult.getEncoded(), externResult.getEncoded());
  }

  public static void ctCryptParam_01(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      String algorithm, int expected) {
    Map<String, String> algorithmParameters =
        cryptographyService.getAlgorithmParameters(spedsLayer, algorithm);

    assertEquals(expected, algorithmParameters.size());
  }

  public static void ctCryptParam_02(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      String algorithm) {
    assertThrows(NoSuchAlgorithmException.class,
        () -> cryptographyService.getAlgorithmParameters(spedsLayer, algorithm));
  }

  public static void ctCryptAlgo_02(CryptographyService cryptographyService, SpedsLayer spedsLayer,
      AlgorithmCategory algorithmCategory) {
    assertThrows(NoSuchCategoryException.class,
        () -> cryptographyService.getAlgorithm(spedsLayer, algorithmCategory));
  }
}
