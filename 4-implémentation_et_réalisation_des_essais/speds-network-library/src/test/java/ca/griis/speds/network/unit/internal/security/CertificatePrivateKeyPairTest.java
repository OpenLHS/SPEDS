
package ca.griis.speds.network.unit.internal.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.griis.speds.network.api.exception.ParameterException;
import ca.griis.speds.network.internal.security.CertificatePrivateKeyPair;
import ca.griis.speds.network.util.X509CertificateCreator;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CertificatePrivateKeyPairTest {
  @BeforeAll
  public static void setupAll() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  @Test
  public void cpkpSuccessRsa() throws Exception {
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

    final var encodedCert = Base64.getEncoder().encodeToString(cert.getEncoded());
    final var encodedKey = Base64.getEncoder().encodeToString(key.getPrivate().getEncoded());
    final var cpkp = CertificatePrivateKeyPair.importFromPem(encodedCert, encodedKey);

    assertEquals(encodedCert, cpkp.getAuthentification());
    assertNotNull(cpkp.getCertificate());
    assertNotNull(cpkp.getPrivateKey());
  }

  @Test
  public void cpkpSuccessEd() throws Exception {
    final KeyPair key = X509CertificateCreator.generateEdKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificate(
        "CN=example.ca",
        "CN=example.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "example.ca",
        "Ed25519");

    final var encodedCert = Base64.getEncoder().encodeToString(cert.getEncoded());
    final var encodedKey = Base64.getEncoder().encodeToString(key.getPrivate().getEncoded());
    final var cpkp = CertificatePrivateKeyPair.importFromPem(encodedCert, encodedKey);

    assertEquals(encodedCert, cpkp.getAuthentification());
    assertNotNull(cpkp.getCertificate());
    assertNotNull(cpkp.getPrivateKey());
  }


  @Test
  public void badCertificate() throws Exception {
    final var encodedCert = Base64.getEncoder().encodeToString(new byte[1]);
    assertThrows(ParameterException.class,
        () -> CertificatePrivateKeyPair.importFromPem(encodedCert, "badkey"));
  }

  @Test
  public void badkeyException() throws Exception {
    final KeyPair key = X509CertificateCreator.generateEdKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificate(
        "CN=example.ca",
        "CN=example.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "example.ca",
        "Ed25519");

    final var encodedCert = Base64.getEncoder().encodeToString(cert.getEncoded());
    assertThrows(ParameterException.class,
        () -> CertificatePrivateKeyPair.importFromPem(encodedCert, "badkey"));
  }
}
