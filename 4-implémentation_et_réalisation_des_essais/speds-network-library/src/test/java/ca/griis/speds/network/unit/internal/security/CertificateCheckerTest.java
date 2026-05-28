
package ca.griis.speds.network.unit.internal.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.speds.network.internal.security.CertificateChecker;
import ca.griis.speds.network.util.X509CertificateCreator;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CertificateCheckerTest {
  private CertificateChecker checker;

  @BeforeAll
  public static void setupAll() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  @BeforeEach
  public void setup() {
    checker = new CertificateChecker();
  }

  @Test
  public void checkSuccess() throws Exception {
    final KeyPair key = X509CertificateCreator.generateRsaKeyPair(1024);
    final X509Certificate cert = X509CertificateCreator.createCertificate(
        "CN=example.ca",
        "CN=example.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "example.ca",
        "SHA256withRSA");

    Boolean isValid = checker.checkCertificate(cert, "https://example.ca:8080");
    assertTrue(isValid);
  }

  @Test
  public void checkFailure() throws Exception {
    final KeyPair key = X509CertificateCreator.generateRsaKeyPair(1024);
    final X509Certificate cert = X509CertificateCreator.createCertificate(
        "CN=example2.ca",
        "CN=example2.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "example2.ca",
        "SHA256withRSA");

    Boolean isValid = checker.checkCertificate(cert, "https://example.ca:8080");
    assertFalse(isValid);
  }
}
