package ca.griis.speds.toolkit.unit.crypto.internal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.speds.toolkit.crypto.api.exception.NoSuchAlgorithmException;
import ca.griis.speds.toolkit.crypto.api.exception.NoSuchCategoryException;
import ca.griis.speds.toolkit.crypto.internal.DefaultCryptographyFactory;
import ca.griis.speds.toolkit.unit.utilities.ConfigCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.security.DrbgParameters;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

public class DefaultCryptographyServiceTest {
  @Test
  void symCipher() throws JsonProcessingException {
    final String json = ConfigCreator.createProfileConfig();
    final var factory = new DefaultCryptographyFactory();
    final var service = factory.initCipherSuite(json);
    final var data = "Ceci est un exemple de chaine de caracteres".getBytes(StandardCharsets.UTF_8);

    final SecretKey key = service.generateSymmetricKey(SpedsConfigItemDto.SpedsLayer.SESSION);
    final var encryptedData =
        service.encryptSymmetric(SpedsConfigItemDto.SpedsLayer.SESSION, key, data);
    final var result =
        service.decryptSymmetric(SpedsConfigItemDto.SpedsLayer.SESSION, key, encryptedData);
    assertArrayEquals(data, result);
  }

  @Test
  void asymCipher() throws Exception {
    final String json = ConfigCreator.createProfileConfig();
    final var factory = new DefaultCryptographyFactory();
    final var service = factory.initCipherSuite(json);
    final var data = "Ceci est un exemple de chaine de caracteres".getBytes(StandardCharsets.UTF_8);

    final var kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
    final var keyPair = kpg.genKeyPair();

    final var encryptedData =
        service.encryptAsymmetric(SpedsConfigItemDto.SpedsLayer.SESSION, keyPair.getPublic(), data);
    final var result =
        service.decryptAsymmetric(SpedsConfigItemDto.SpedsLayer.SESSION, keyPair.getPrivate(),
            encryptedData);
    assertArrayEquals(data, result);
  }

  @Test
  void hash() throws JsonProcessingException {
    final String json = ConfigCreator.createProfileConfig();
    final var factory = new DefaultCryptographyFactory();
    final var service = factory.initCipherSuite(json);
    final var data = "Ceci est un exemple de chaine de caracteres".getBytes(StandardCharsets.UTF_8);

    final var result = service.hash(SpedsLayer.TRANSPORT, data);
    assertTrue(result.length > 0);
  }

  @Test
  void signature() throws Exception {
    final String json = ConfigCreator.createProfileConfig();
    final var factory = new DefaultCryptographyFactory();
    final var service = factory.initCipherSuite(json);
    final var data = "Ceci est un exemple de chaine de caracteres".getBytes(StandardCharsets.UTF_8);

    final var kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
    final var keyPair = kpg.genKeyPair();

    var signature = service.sign(SpedsLayer.NETWORK, keyPair.getPrivate(), data);
    var result =
        service.checkSignatureValidity(SpedsLayer.NETWORK, signature, keyPair.getPublic(), data);
    assertTrue(result);
  }

  @Test
  void dh() throws JsonProcessingException {
    final String json = ConfigCreator.createProfileConfig();
    final var factory = new DefaultCryptographyFactory();
    final var service = factory.initCipherSuite(json);
    final var data = "Ceci est un exemple de chaine de caracteres".getBytes(StandardCharsets.UTF_8);

    var originatorKeyPair = service.chooseDiffieHellmanValue(SpedsLayer.SESSION);
    var recipientKeyPair = service.chooseDiffieHellmanValue(SpedsLayer.SESSION);
    var key = service.getDiffieHellmanSecretKey(SpedsLayer.SESSION, originatorKeyPair,
        recipientKeyPair.getPublic());
    final var encryptedData =
        service.encryptSymmetric(SpedsConfigItemDto.SpedsLayer.SESSION, key, data);
    final var result =
        service.decryptSymmetric(SpedsConfigItemDto.SpedsLayer.SESSION, key, encryptedData);
    assertArrayEquals(data, result);
  }

  @Test
  void getAlgorithmLayers() throws JsonProcessingException {
    final var json = ConfigCreator.createProfileConfig();
    final var factory = new DefaultCryptographyFactory();
    final var service = factory.initCipherSuite(json);

    assertThrows(NoSuchCategoryException.class, () -> {
      service.getAlgorithm(SpedsLayer.APPLICATION, AlgorithmCategory.SYMM);
    });

    var result = service.getAlgorithm(SpedsLayer.PRESENTATION, AlgorithmCategory.SYMM);
    assertEquals(result, "AES/GCM/NoPadding");

    result = service.getAlgorithm(SpedsLayer.SESSION, AlgorithmCategory.SYMM);
    assertEquals(result, "AES/GCM/NoPadding");

    result = service.getAlgorithm(SpedsLayer.SESSION, AlgorithmCategory.HASH);
    assertEquals(result, "SHA-512");

    result = service.getAlgorithm(SpedsLayer.SESSION, AlgorithmCategory.SIGN);
    assertEquals(result, "RSASSA-PSS");

    result = service.getAlgorithm(SpedsLayer.SESSION, AlgorithmCategory.DH);
    assertEquals(result, "X25519");

    result = service.getAlgorithm(SpedsLayer.TRANSPORT, AlgorithmCategory.HASH);
    assertEquals(result, "SHA-512");

    result = service.getAlgorithm(SpedsLayer.NETWORK, AlgorithmCategory.HASH);
    assertEquals(result, "SHA-512");

    result = service.getAlgorithm(SpedsLayer.NETWORK, AlgorithmCategory.SIGN);
    assertEquals(result, "RSASSA-PSS");
  }

  @Test
  void getAlgorithmParameters() throws JsonProcessingException {
    final var json = ConfigCreator.createProfileConfig();
    final var factory = new DefaultCryptographyFactory();
    final var service = factory.initCipherSuite(json);

    assertThrows(NoSuchAlgorithmException.class, () -> {
      service.getAlgorithmParameters(SpedsLayer.SESSION, "?");
    });

    // DRBG
    final var drbgParams = service.getAlgorithmParameters(SpedsLayer.SESSION, "DRBG");
    assertTrue(drbgParams.size() > 2);

    final var bitStrength = Integer.parseInt(drbgParams.get("strength"));
    final var capability = drbgParams.get("capability");
    assertEquals(bitStrength, 256);
    assertEquals(capability, DrbgParameters.Capability.PR_AND_RESEED.toString());

    // SHA-512
    final var sha512 = service.getAlgorithmParameters(SpedsLayer.SESSION, "SHA-512");
    assertEquals(sha512.size(), 0);

    // AES
    final var aes = service.getAlgorithmParameters(SpedsLayer.SESSION, "AES");
    assertEquals(aes.size(), 1);

    final var keyBitLength = Integer.parseInt(aes.get("keyBitLength"));
    assertEquals(keyBitLength, 256);

    // AES/GCM/NoPadding
    final var gcmAes = service.getAlgorithmParameters(SpedsLayer.SESSION, "AES/GCM/NoPadding");
    assertEquals(gcmAes.size(), 2);

    final var gcmTagBitSize = Integer.parseInt(gcmAes.get("gcmTagBitSize"));
    assertEquals(gcmTagBitSize, 128);

    final var gcmIvBitLength = Integer.parseInt(gcmAes.get("gcmIvBitLength"));
    assertEquals(gcmIvBitLength, 96);

    // RSA
    final var rsa = service.getAlgorithmParameters(SpedsLayer.NETWORK, "RSA");
    assertEquals(rsa.size(), 2);

    final var rsaKeyBitLength = Integer.parseInt(rsa.get("keysize"));
    assertEquals(rsaKeyBitLength, 4096);

    final var rsaPublicExponent = Integer.parseInt(rsa.get("publicExponent"));
    assertEquals(rsaPublicExponent, 65537);

    // RSA
    final var sesRsa = service.getAlgorithmParameters(SpedsLayer.SESSION, "RSA");
    assertEquals(sesRsa.size(), 2);

    final var sesRsaKeyBitLength = Integer.parseInt(sesRsa.get("keysize"));
    assertEquals(sesRsaKeyBitLength, 4096);

    final var sesRsaPublicExponent = Integer.parseInt(sesRsa.get("publicExponent"));
    assertEquals(sesRsaPublicExponent, 65537);

    // RSA/ECB/OAEPWithSHA-256AndMGF1Padding
    var rsaCipher =
        service.getAlgorithmParameters(SpedsLayer.SESSION, "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    assertEquals(rsaCipher.size(), 4);

    final var cipherMgfSpec = rsaCipher.get("mgfSpec");
    assertEquals(cipherMgfSpec, "SHA-512");

    final var cipherMgfName = rsaCipher.get("mgfName");
    assertEquals(cipherMgfName, "MGF1");

    final var cipherMdName = rsaCipher.get("mdName");
    assertEquals(cipherMdName, "SHA-512");

    final var pSrc = rsaCipher.get("pSrc");
    assertEquals(pSrc, "");

    // X25519
    var dh = service.getAlgorithmParameters(SpedsLayer.SESSION, "X25519");
    assertEquals(dh.size(), 3);

    final var dhkeyBitLength = Integer.parseInt(dh.get("keyBitLength"));
    assertEquals(dhkeyBitLength, 256);

    // RSASSA-PSS
    var rsaPss = service.getAlgorithmParameters(SpedsLayer.NETWORK, "RSASSA-PSS");
    assertEquals(rsaPss.size(), 5);

    final var mgfSpec = rsaPss.get("mgfSpec");
    assertEquals(mgfSpec, "SHA-512");

    final var mgfName = rsaPss.get("mgfName");
    assertEquals(mgfName, "MGF1");

    final var trailerField = Integer.parseInt(rsaPss.get("trailerField"));
    assertEquals(trailerField, 1);

    final var mdName = rsaPss.get("mdName");
    assertEquals(mdName, "SHA-512");

    final var saltLen = Integer.parseInt(rsaPss.get("saltLen"));
    assertEquals(saltLen, 64);
  }
}
