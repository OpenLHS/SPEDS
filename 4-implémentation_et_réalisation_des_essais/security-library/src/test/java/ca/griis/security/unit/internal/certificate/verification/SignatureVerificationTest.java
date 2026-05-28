package ca.griis.security.unit.internal.certificate.verification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.security.api.service.DefaultSecurityService;
import ca.griis.security.unit.util.X509CertificateCreator;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SignatureVerificationTest {
  private DefaultSecurityService signatureVerification;
  private KeyPair issuerKeyPair;
  private KeyPair subjectKeyPair;
  private KeyPair wrongKeyPair;

  @BeforeEach
  void setUp() throws Exception {
    signatureVerification = new DefaultSecurityService();

    // Générer les paires de clés pour les tests
    issuerKeyPair = X509CertificateCreator.generateKeyPair();
    subjectKeyPair = X509CertificateCreator.generateKeyPair();
    wrongKeyPair = X509CertificateCreator.generateKeyPair();
  }

  @Test
  void testValidSignature() throws Exception {
    // Arrange - Certificat signé par issuerKeyPair
    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=Valid Signature Certificate",
        "CN=Root CA",
        subjectKeyPair.getPublic(),
        issuerKeyPair.getPublic(),
        issuerKeyPair.getPrivate(),
        false);

    // Act - Vérifier avec la bonne clé publique
    Boolean result = signatureVerification.verifyCertificateSignature(
        certificate,
        issuerKeyPair.getPublic());

    // Assert
    assertTrue(result);
  }

  @Test
  void testInvalidSignatureWithWrongKey() throws Exception {
    // Arrange - Certificat signé par issuerKeyPair
    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=Invalid Signature Certificate",
        "CN=Root CA",
        subjectKeyPair.getPublic(),
        issuerKeyPair.getPublic(),
        issuerKeyPair.getPrivate(),
        false);

    // Act - Vérifier avec une mauvaise clé publique
    Boolean result = signatureVerification.verifyCertificateSignature(
        certificate,
        wrongKeyPair.getPublic());

    // Assert
    assertFalse(result);
  }

  @Test
  void testExpiredCertificateWithValidSignature() throws Exception {
    // Arrange - Certificat expiré mais avec signature valide
    X509Certificate expiredCertificate = X509CertificateCreator.createExpiredCertificate(
        "CN=Expired But Valid Signature",
        "CN=Expired But Valid Signature",
        subjectKeyPair.getPublic(),
        issuerKeyPair.getPublic(),
        issuerKeyPair.getPrivate(),
        false);

    // Act - La signature devrait quand même être valide même si le certificat est expiré
    Boolean result = signatureVerification.verifyCertificateSignature(
        expiredCertificate,
        issuerKeyPair.getPublic());

    // Assert
    assertTrue(result);
  }

  @Test
  void testCertificateChain() throws Exception {
    // Arrange - Créer un certificat root CA
    X509Certificate rootCA = X509CertificateCreator.createCertificate(
        "CN=Root CA",
        "CN=Root CA",
        issuerKeyPair.getPublic(),
        issuerKeyPair.getPublic(),
        issuerKeyPair.getPrivate(),
        true);

    // Créer un certificat intermédiaire signé par root CA
    KeyPair intermediateKeyPair = X509CertificateCreator.generateKeyPair();
    X509Certificate intermediateCert = X509CertificateCreator.createCertificate(
        "CN=Intermediate CA",
        "CN=Root CA",
        intermediateKeyPair.getPublic(),
        issuerKeyPair.getPublic(),
        issuerKeyPair.getPrivate(),
        true);

    // Créer un certificat final signé par intermediate CA
    X509Certificate endCertificate = X509CertificateCreator.createCertificate(
        "CN=End Entity",
        "CN=Root CA",
        subjectKeyPair.getPublic(),
        intermediateKeyPair.getPublic(),
        intermediateKeyPair.getPrivate(),
        false);

    // Act & Assert - Vérifier chaque maillon de la chaîne
    assertTrue(
        signatureVerification.verifyCertificateSignature(rootCA, issuerKeyPair.getPublic()));

    assertTrue(
        signatureVerification.verifyCertificateSignature(intermediateCert,
            issuerKeyPair.getPublic()));

    assertTrue(
        signatureVerification.verifyCertificateSignature(endCertificate,
            intermediateKeyPair.getPublic()));
  }
}
