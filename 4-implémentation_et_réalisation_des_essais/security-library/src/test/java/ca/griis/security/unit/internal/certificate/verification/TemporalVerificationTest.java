package ca.griis.security.unit.internal.certificate.verification;

import static org.junit.jupiter.api.Assertions.*;

import ca.griis.security.api.service.DefaultSecurityService;
import ca.griis.security.unit.util.X509CertificateCreator;
import java.security.*;
import java.security.cert.X509Certificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemporalVerificationTest {

  private KeyPair keyPair;
  private DefaultSecurityService temporalVerification = new DefaultSecurityService();

  @BeforeEach
  void setUp() throws Exception {
    // Générer une paire de clés pour les tests
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    keyPair = keyGen.generateKeyPair();
  }

  @Test
  public void testCertificateValid() throws Exception {
    // Arrange
    X509Certificate validCertificate = X509CertificateCreator.createCertificate(
        "CN=RootCA",
        "CN=Root CA",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        true);

    // Act
    Boolean result = temporalVerification.verifyCertificateTemporalValidity(validCertificate);

    // Assert
    assertTrue(result);
  }

  @Test
  void testCertificateExpired() throws Exception {
    // Arrange
    X509Certificate expiredCertificate = X509CertificateCreator.createExpiredCertificate(
        "CN=Test Expired Certificate",
        "CN=Root CA",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        true);

    // Act
    Boolean result = temporalVerification.verifyCertificateTemporalValidity(expiredCertificate);

    // Assert
    assertFalse(result);
  }

  @Test
  void testCertificateNotYetValid() throws Exception {
    // Arrange
    X509Certificate notYetValidCertificate = X509CertificateCreator.createNotYetValidCertificate(
        "CN=Test Not Yet Valid Certificate",
        "CN=Root CA",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        true);

    // Act
    Boolean result = temporalVerification.verifyCertificateTemporalValidity(notYetValidCertificate);

    // Assert
    assertFalse(result);
  }
}
