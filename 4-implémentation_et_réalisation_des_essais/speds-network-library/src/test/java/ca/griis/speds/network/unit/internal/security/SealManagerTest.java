package ca.griis.speds.network.unit.internal.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.speds.network.internal.security.SealManager;
import ca.griis.speds.network.util.SecurityUtils;
import ca.griis.speds.network.util.X509CertificateCreator;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import java.security.KeyPair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SealManagerTest {
  private static KeyPair keys;

  private SealManager sealManager;

  @BeforeAll
  public static void setupAll() throws Exception {
    keys = X509CertificateCreator.generateRsaKeyPair(4096);
  }

  @BeforeEach
  public void setUp() throws Exception {
    CryptographyService service = SecurityUtils.createCryptographyService();
    sealManager = new SealManager(service);
  }

  @Test
  public void createCheckSuccess() throws Exception {
    final var msg = "message1";

    String seal = sealManager.createSeal(msg, keys.getPrivate());
    Boolean isValid = sealManager.checkSeal(msg, keys.getPublic(), seal);
    assertTrue(isValid);
  }

  @Test
  public void createCheckFailed() throws Exception {
    final var msg = "message1";

    String seal = sealManager.createSeal(msg, keys.getPrivate());
    Boolean isValid = sealManager.checkSeal("message2", keys.getPublic(), seal);
    assertFalse(isValid);
  }
}
