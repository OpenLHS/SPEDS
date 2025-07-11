package ca.griis.speds.session.unit.internal.service.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.cryptography.keyexchange.X25519Provider;
import ca.griis.speds.session.api.exception.KeyAgreementException;
import ca.griis.speds.session.internal.service.crypto.KeyAgreement;
import ca.griis.speds.session.internal.util.KeyAlgorithm;
import ca.griis.speds.session.internal.util.KeyMapping;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

public class KeyAgreementTest {

  KeyAgreement keyAgreementMgr;

  @BeforeEach
  public void setUp() throws Exception {
    keyAgreementMgr = new KeyAgreement();
  }

  @Test
  public void testGenerateChoicePointKey() {
    KeyPair kp = keyAgreementMgr.generateChoicePointKey();
    assertNotNull(kp);
    assertNotNull(kp.getPrivate());
    assertNotNull(kp.getPublic());
  }

  @Test
  public void testCompleteKeyAgreementNegotiation_succes() {
    KeyPair kpX = keyAgreementMgr.generateChoicePointKey();
    KeyPair kpY = keyAgreementMgr.generateChoicePointKey();

    byte[] skak = keyAgreementMgr.completeKeyAgreementNegotiation(kpX, kpY.getPublic());

    assertNotNull(skak);
    SecretKey skSkak = KeyMapping.getAesSecretKeyFromByte(skak);
    assertEquals("AES", skSkak.getAlgorithm());
  }

  @Test
  public void testCompleteKeyAgreementNegotiation_invalidKey() {
    KeyPair kpX = keyAgreementMgr.generateChoicePointKey();
    String pk =
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQtbl/3itKib0fv3FGU29ma9N8FH8IuCBcqROAy757DkJURp1LhAWyugi959EEPIdRpM8dORYoOMLmF3Pnzrqkjbvyl2rYGZ1RX01UPeZ8qIv9wXiGzui9NCzdFDrynRd5zvWiyFDSC3eGj3N7BYRfsUiE3mjhc9wY6Y2IgotCSwIDAQAB";
    PublicKey invalid = KeyMapping.getPublicKeyFromString(pk, KeyAlgorithm.RSA);

    assertThrows(KeyAgreementException.class, () -> {
      keyAgreementMgr.completeKeyAgreementNegotiation(kpX, invalid);
    });
  }

  @Test
  public void testKeyAgreementConstructor() {
    try (MockedConstruction<X25519Provider> mocked =
        Mockito.mockConstructionWithAnswer(X25519Provider.class, invocation -> {
          throw new NoSuchAlgorithmException();
        })) {
      new KeyAgreement();
    }
  }
}
