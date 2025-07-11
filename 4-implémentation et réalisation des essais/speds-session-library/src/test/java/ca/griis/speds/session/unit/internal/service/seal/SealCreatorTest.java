package ca.griis.speds.session.unit.internal.service.seal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import ca.griis.cryptography.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.cryptography.symmetric.generator.SecretKeyGenerator;
import ca.griis.speds.session.api.exception.CipherException;
import ca.griis.speds.session.internal.service.seal.SealCreator;
import ca.griis.speds.session.internal.util.KeyAlgorithm;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SealCreatorTest {

  private SealCreator seal;

  @Spy
  private ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();
  private PrivateKey privateKey;

  @BeforeEach
  public void setUp() throws Exception {
    seal = new SealCreator();
    String privateKeyPem =
        "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";
    final byte[] encoded = Base64.getDecoder().decode(privateKeyPem);
    final KeyFactory keyFactory = KeyFactory.getInstance(KeyAlgorithm.RSA.name());
    final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    privateKey = keyFactory.generatePrivate(keySpec);
  }

  @Test
  public void testCreateSeal_jsonProcessingError() throws Exception {
    Object tobesealed = "someObject";
    doThrow(JsonProcessingException.class).when(mapper).writeValueAsBytes(any());

    assertThrows(CipherException.class, () -> {
      seal.createSeal(tobesealed, privateKey, mapper);
    });
  }

  @Test
  public void testCreateSymmetricalSeal_jsonProcessingError() throws Exception {
    Object tobesealed = "someObject";
    doThrow(JsonProcessingException.class).when(mapper).writeValueAsBytes(any());

    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);

    assertThrows(CipherException.class, () -> {
      seal.createSymmetricalSeal(tobesealed, secretKey, mapper);
    });
  }
}
