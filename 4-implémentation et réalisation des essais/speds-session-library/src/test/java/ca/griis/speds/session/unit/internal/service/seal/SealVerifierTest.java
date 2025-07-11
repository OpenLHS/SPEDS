package ca.griis.speds.session.unit.internal.service.seal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import ca.griis.cryptography.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.cryptography.symmetric.generator.SecretKeyGenerator;
import ca.griis.speds.session.internal.service.seal.SealVerifier;
import ca.griis.speds.session.internal.util.KeyMapping;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SealVerifierTest {

  private SealVerifier seal;

  @Spy
  private ObjectMapper mapper = SharedObjectMapper.getInstance().getMapper();
  private String publicKey;

  @BeforeEach
  public void setUp() throws Exception {
    seal = new SealVerifier();
    publicKey =
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAgKtO2rClnPByuuzW4KwDQ3lbb9NYOdd5wO4y7yxjMmd7j1SjB8ztG/aydfQRscFWfXCRRzcJHQtXhi4yXcmI1k9fY15ASmNU6g0QzP8QsvqAzgjVwJTMKbuznT4uATzkhtMXW+q5BdRmzJmYuwPtIAzAOVy96yZoDjwvq7CP39p9XuE2CqDeqXek4dpocdiu0PPCgOD/+W4iQ+XC5rUyv+14Y00dsTPDsw3o79Rc/pVb5L9MQFywm3qxkqxUy8wNCdwCim2oJqquZ9q+/yqqCC1MHz5LA7lbVGnP+xY1uQdPpSwDMWHZW74eC09xmveePoxJwoDMPTzh4KuL4BQkKj4JJ2VH00XT0sylywkv9eT9HPAL5djX4SADc5b9QliiZ6JFQQ8RF4Wg3cS5MfCN6Gvm2EOsI0/qZB8wsxsEAGPtYalFvnrTPvkAHlyeirUxpa686w40GeRGgqQB+Te4OK3mDxVTSduDSfOyOCKTM0B6iVF+xv/2QyR7gQ7Nbozd3aIA1HlDEMobxWsMJbooXPZ94jTt+ekcIIdIqMlNSe0HZKo2NSilMa395FdWQpzPgyacu8UVzl1z2wb+X0zVsRyBOweJ1Y6XdUiAUCxVikYAsuBseJoA/OWMGObMaSYS1MWJegiOptAfFTK1qVV+HztO89J4U6EQMxoO8k5JZ/cCAwEAAQ==";
  }


  @Test
  public void testVerifySeal_badseal() {
    assertFalse(seal.verifySeal(mapper, "someObject", publicKey, ""));
  }

  @Test
  public void testVerifySeal_badkey() {
    String badKey =
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA6GieDi245HfxRs1838LeksyDroaqgu/vwDLFFEmP/MsZUjE/d0Ic/uFaVulb+J9OzPV8BtjZ7nfc4Xbfyk7VVhaPpRGunvuU0HHV3p9SMbhVEf2dmDg2RD87Rqn041dKor1b2VJjKHCZa5cvvq0sxQCCh3c7y/Y4eFLzPUwjhdYKosiZhOw4dsRkYALWTjDhMDfTkTxHOHpfkNPrBN8Ej9kTDyZrA0yz7nlWqXurbTda3a3MK2x2/Injj28396y6iHirygxVATs8wJ27Y4EWH1NFXM3xuWu6pgN00iEyGYs0Kp4Xs2wPL9FJxpFwf211HWWi78R0c3oQ/mwbAZX+0VLwI10RaDDv5OgAH768Wv2nviFS29MCm13YshJvtbkiaDfBHSXJsSoWEGjRqdYILkUDnRmEhspWljNIa5cmSBpcKkssl5ioOOdghVS6ADkTjWe5durvUYCXuNs1/ASitWLs084z6FZji+o4WBl80f+WLLKF4IFrK42XE/tj+geba7KpVbxQlTIKQGP1mVE5DdkVwrpK/3sJwMn4BKUn8/mZKaroFuLVHpmSab8v7RtjRm56HWSWv/leCSzVWXT0CI6ZjIL8n0RfKCIwEf4kW871grM2xPTbZnZ5/kg/TWARTgwSB7TJBwQ4OjfyV8VMJ0vhr3Y8VxHDP/ITfEcv1EECAwEAAQ==";
    assertFalse(seal.verifySeal(mapper, "someObject", badKey, "a good seal"));
  }

  @Test
  public void testVerifySymmetricalSeal_jsonProcessingError() throws Exception {
    doThrow(JsonProcessingException.class).when(mapper).writeValueAsBytes(any());
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    assertFalse(seal.verifySymmetricalSeal("someObject", secretKey, "s", mapper));
  }

  @Test
  public void testVerifySymmetricalSeal_badkey() throws Exception {
    String badKey =
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA6GieDi245HfxRs1838LeksyDroaqgu/vwDLFFEmP/MsZUjE/d0Ic/uFaVulb+J9OzPV8BtjZ7nfc4Xbfyk7VVhaPpRGunvuU0HHV3p9SMbhVEf2dmDg2RD87Rqn041dKor1b2VJjKHCZa5cvvq0sxQCCh3c7y/Y4eFLzPUwjhdYKosiZhOw4dsRkYALWTjDhMDfTkTxHOHpfkNPrBN8Ej9kTDyZrA0yz7nlWqXurbTda3a3MK2x2/Injj28396y6iHirygxVATs8wJ27Y4EWH1NFXM3xuWu6pgN00iEyGYs0Kp4Xs2wPL9FJxpFwf211HWWi78R0c3oQ/mwbAZX+0VLwI10RaDDv5OgAH768Wv2nviFS29MCm13YshJvtbkiaDfBHSXJsSoWEGjRqdYILkUDnRmEhspWljNIa5cmSBpcKkssl5ioOOdghVS6ADkTjWe5durvUYCXuNs1/ASitWLs084z6FZji+o4WBl80f+WLLKF4IFrK42XE/tj+geba7KpVbxQlTIKQGP1mVE5DdkVwrpK/3sJwMn4BKUn8/mZKaroFuLVHpmSab8v7RtjRm56HWSWv/leCSzVWXT0CI6ZjIL8n0RfKCIwEf4kW871grM2xPTbZnZ5/kg/TWARTgwSB7TJBwQ4OjfyV8VMJ0vhr3Y8VxHDP/ITfEcv1EECAwEAAQ==";
    SecretKey secret = KeyMapping.getAesSecretKeyFromByte(Base64.getDecoder().decode(badKey));
    assertFalse(seal.verifySymmetricalSeal("someObject", secret, "s", mapper));
  }

  @Test
  public void testVerifySymmetricalSeal_badSeal() throws Exception {
    SecretKey secretKey =
        SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
    assertFalse(seal.verifySymmetricalSeal("someObject", secretKey, "badseal", mapper));
  }
}
