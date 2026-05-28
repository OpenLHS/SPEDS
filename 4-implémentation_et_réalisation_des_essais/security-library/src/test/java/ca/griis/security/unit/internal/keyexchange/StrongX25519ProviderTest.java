package ca.griis.security.unit.internal.keyexchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.domain.spec.csprng.StrongCsprngSpec;
import ca.griis.security.api.exception.DecryptException;
import ca.griis.security.internal.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.security.internal.encryption.Decryptor;
import ca.griis.security.internal.encryption.Encryptor;
import ca.griis.security.internal.keyexchange.KeyAgreementProvider;
import ca.griis.security.internal.keyexchange.X25519Provider;
import ca.griis.security.internal.symmetric.encryption.AesGcmDecryptor;
import ca.griis.security.internal.symmetric.encryption.AesGcmEncryptor;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StrongX25519ProviderTest {
  private PublicKey alicePublicKey;

  private Encryptor aliceEncryptor;

  private Decryptor aliceDecryptor;

  private Encryptor bobEncryptor;

  private Decryptor bobDecryptor;

  @BeforeEach
  public void setUp() throws Exception {
    CsprngSpec csprngSpec = new StrongCsprngSpec();
    KeyAgreementProvider aliceEcdhProvider = new X25519Provider();
    final KeyPair aliceKeyPair = aliceEcdhProvider.generateEphemeralKeys();
    final PrivateKey alicePrivateKey = aliceKeyPair.getPrivate();
    alicePublicKey = aliceKeyPair.getPublic();

    KeyAgreementProvider bobEcdhProvider = new X25519Provider();
    final KeyPair bobKeyPair = bobEcdhProvider.generateEphemeralKeys();
    final PrivateKey bobPrivateKey = bobKeyPair.getPrivate();
    final PublicKey bobPublicKey = bobKeyPair.getPublic();

    aliceEcdhProvider.initializeAgreement(alicePrivateKey, csprngSpec);
    byte[] sharedKeyForAlice = aliceEcdhProvider.completeAgreement(bobPublicKey);
    byte[] derivedKeyForAlice =
        aliceEcdhProvider.deriveSecure256BitsKey(sharedKeyForAlice, alicePublicKey, bobPublicKey);
    SecretKey secretKeyForAlice =
        new SecretKeySpec(derivedKeyForAlice, SecretKeyGeneratorAlgorithm.AES.getAlgorithm());
    aliceEncryptor = new AesGcmEncryptor(secretKeyForAlice);
    aliceDecryptor = new AesGcmDecryptor(secretKeyForAlice);

    bobEcdhProvider.initializeAgreement(bobPrivateKey, csprngSpec);
    byte[] sharedKeyForBob = bobEcdhProvider.completeAgreement(alicePublicKey);
    byte[] derivedKeyFoBob =
        bobEcdhProvider.deriveSecure256BitsKey(sharedKeyForBob, bobPublicKey, alicePublicKey);
    SecretKey secretKeyForBob =
        new SecretKeySpec(derivedKeyFoBob, SecretKeyGeneratorAlgorithm.AES.getAlgorithm());
    bobEncryptor = new AesGcmEncryptor(secretKeyForBob);
    bobDecryptor = new AesGcmDecryptor(secretKeyForBob);
  }

  @Test
  public void encryptedMessageFromAliceToBob() throws Exception {
    CsprngSpec csprngSpec = new StrongCsprngSpec();
    final String expected = "This is a message from Alice to Bob; Hello !";

    byte[] expectedByte = expected.getBytes(StandardCharsets.UTF_8);
    byte[] encryptedData = aliceEncryptor.encrypt(expectedByte, csprngSpec);
    assertThat(encryptedData).isNotEqualTo(expectedByte);

    byte[] decryptedData = bobDecryptor.decrypt(encryptedData, csprngSpec);
    assertThat(decryptedData).isEqualTo(expectedByte);
    assertThat(new String(decryptedData, StandardCharsets.UTF_8)).isEqualTo(expected);
  }

  @Test
  public void encryptedMessageFromBobToAlice() throws Exception {
    CsprngSpec csprngSpec = new StrongCsprngSpec();
    final String expected = "This is a message from Bob to Alice; Hello to you too !";

    byte[] expectedByte = expected.getBytes(Charset.forName("UTF-8"));
    byte[] encryptedData = bobEncryptor.encrypt(expectedByte, csprngSpec);
    assertThat(encryptedData).isNotEqualTo(expectedByte);

    byte[] decryptedData = aliceDecryptor.decrypt(encryptedData, csprngSpec);
    assertThat(decryptedData).isEqualTo(expectedByte);
    assertThat(new String(decryptedData, StandardCharsets.UTF_8)).isEqualTo(expected);
  }

  @Test
  public void encryptedMessageIntercepted() throws Exception {
    CsprngSpec csprngSpec = new StrongCsprngSpec();
    KeyAgreementProvider ecdhProviderEve = new X25519Provider();
    final KeyPair eveKeyPair = ecdhProviderEve.generateEphemeralKeys();
    final PrivateKey evePrivateKey = eveKeyPair.getPrivate();
    final PublicKey evePublicKey = eveKeyPair.getPublic();

    ecdhProviderEve.initializeAgreement(evePrivateKey, csprngSpec);
    byte[] sharedKeyForEve = ecdhProviderEve.completeAgreement(alicePublicKey);
    byte[] derivedKeyForEve =
        ecdhProviderEve.deriveSecure256BitsKey(sharedKeyForEve, evePublicKey, alicePublicKey);
    SecretKey secretKeyForEve =
        new SecretKeySpec(derivedKeyForEve, SecretKeyGeneratorAlgorithm.AES.getAlgorithm());
    Decryptor eveDecryptor = new AesGcmDecryptor(secretKeyForEve);

    final String expected = "This is a message from Alice to Bob; Hello !";

    byte[] expectedByte = expected.getBytes(StandardCharsets.UTF_8);
    byte[] encryptedData = aliceEncryptor.encrypt(expectedByte, csprngSpec);
    assertThat(encryptedData).isNotEqualTo(expectedByte);

    Throwable thrown = catchThrowable(() -> eveDecryptor.decrypt(encryptedData, csprngSpec));

    assertThat(thrown).isInstanceOf(DecryptException.class);
  }
}
