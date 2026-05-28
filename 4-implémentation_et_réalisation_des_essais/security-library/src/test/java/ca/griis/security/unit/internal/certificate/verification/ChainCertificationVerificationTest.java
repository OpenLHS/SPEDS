package ca.griis.security.unit.internal.certificate.verification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.security.api.domain.spec.certificate.CertificateVerifSpec;
import ca.griis.security.api.domain.spec.certificate.IntermediateCertificates;
import ca.griis.security.api.domain.spec.certificate.RootCertificates;
import ca.griis.security.api.service.DefaultSecurityService;
import ca.griis.security.unit.util.X509CertificateCreator;
import java.security.KeyPair;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChainCertificationVerificationTest {
  private DefaultSecurityService chainCertificationVerification;
  private KeyPair rootKeyPair;
  private KeyPair intermediateKeyPair;
  private KeyPair endEntityKeyPair;
  private KeyPair wrongKeyPair;
  private X509Certificate rootCert;
  private X509Certificate intermediateCert;

  @BeforeEach
  void setUp() throws Exception {
    // Générer les paires de clés pour la chaîne
    rootKeyPair = X509CertificateCreator.generateKeyPair();
    intermediateKeyPair = X509CertificateCreator.generateKeyPair();
    endEntityKeyPair = X509CertificateCreator.generateKeyPair();
    wrongKeyPair = X509CertificateCreator.generateKeyPair();
    rootCert = X509CertificateCreator.createCertificate(
        "CN=Root CA",
        "CN=Root CA",
        rootKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);

    intermediateCert = X509CertificateCreator.createCertificate(
        "CN=Intermediate CA",
        "CN=Root CA",
        intermediateKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);

    final X509CRL rootCrl = X509CertificateCreator.createCRL(
        "CN=Root CA",
        rootKeyPair.getPrivate(),
        rootCert,
        null);

    final X509CRL intermediateCRL = X509CertificateCreator.createCRL(
        "CN=Intermediate CA",
        intermediateKeyPair.getPrivate(),
        intermediateCert,
        null);

    chainCertificationVerification = new DefaultSecurityService(List.of(rootCrl, intermediateCRL));
  }

  @Test
  void testValidCertificateChain() throws Exception {
    final var endEntityCert = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=Intermediate CA",
        endEntityKeyPair.getPublic(),
        intermediateKeyPair.getPublic(),
        intermediateKeyPair.getPrivate(),
        true);

    CertificateVerifSpec spec = new CertificateVerifSpec();
    RootCertificates rootCerts = new RootCertificates(List.of(rootCert));
    IntermediateCertificates intermediateCerts =
        new IntermediateCertificates(new ArrayList<>(List.of(intermediateCert)));

    // Act
    Boolean result = chainCertificationVerification.verifyCertificateChain(
        spec,
        rootCerts,
        intermediateCerts,
        endEntityCert);

    // Assert
    assertTrue(result);
  }

  @Test
  void testValidChainWithoutIntermediate() throws Exception {
    // Arrange - Certificat signé directement par root
    X509Certificate endEntityCert = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=Root CA", // Signé directement par Root CA
        endEntityKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        false);

    CertificateVerifSpec spec = new CertificateVerifSpec();
    RootCertificates rootCerts = new RootCertificates(List.of(rootCert));
    IntermediateCertificates intermediateCerts = new IntermediateCertificates(new ArrayList<>());

    // Act
    Boolean result = chainCertificationVerification.verifyCertificateChain(
        spec,
        rootCerts,
        intermediateCerts,
        endEntityCert);

    // Assert
    assertTrue(result, "Un certificat signé directement par le root devrait être valide");
  }

  @Test
  void testValidChainWithMultipleRoots() throws Exception {
    // Arrange - Plusieurs root CA, un seul est le bon
    X509Certificate correctRootCert = X509CertificateCreator.createCertificate(
        "CN=Correct Root CA",
        "CN=Correct Root CA",
        rootKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);

    KeyPair otherRootKeyPair = X509CertificateCreator.generateKeyPair();
    X509Certificate otherRootCert = X509CertificateCreator.createCertificate(
        "CN=Other Root CA",
        "CN=Other Root CA",
        otherRootKeyPair.getPublic(),
        otherRootKeyPair.getPublic(),
        otherRootKeyPair.getPrivate(),
        true);

    X509Certificate intermediateCert = X509CertificateCreator.createCertificate(
        "CN=Intermediate CA",
        "CN=Correct Root CA",
        intermediateKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);

    X509Certificate endEntityCert = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=Intermediate CA",
        endEntityKeyPair.getPublic(),
        intermediateKeyPair.getPublic(),
        intermediateKeyPair.getPrivate(),
        false);

    final X509CRL rootCrl = X509CertificateCreator.createCRL(
        "CN=Correct Root CA",
        rootKeyPair.getPrivate(),
        correctRootCert,
        null);

    final X509CRL otherRootCrl = X509CertificateCreator.createCRL(
        "CN=Other Root CA",
        otherRootKeyPair.getPrivate(),
        otherRootCert,
        null);

    final X509CRL intermediateCRL = X509CertificateCreator.createCRL(
        "CN=Intermediate CA",
        intermediateKeyPair.getPrivate(),
        intermediateCert,
        null);

    chainCertificationVerification =
        new DefaultSecurityService(List.of(rootCrl, otherRootCrl, intermediateCRL));

    CertificateVerifSpec spec = new CertificateVerifSpec();
    RootCertificates rootCerts = new RootCertificates(List.of(correctRootCert, otherRootCert));
    IntermediateCertificates intermediateCerts = new IntermediateCertificates(
        new ArrayList<>(List.of(intermediateCert)));

    // Act
    Boolean result = chainCertificationVerification.verifyCertificateChain(
        spec,
        rootCerts,
        intermediateCerts,
        endEntityCert);

    // Assert
    assertTrue(result, "La validation devrait réussir avec le bon root parmi plusieurs");
  }

  // ============================================================
  // Tests de chaîne invalide
  // ============================================================

  @Test
  void testInvalidChainWrongIntermediate() throws Exception {
    // Arrange - Certificat signé par une mauvaise clé
    X509Certificate intermediateCert = X509CertificateCreator.createCertificate(
        "CN=Intermediate CA",
        "CN=Root CA",
        intermediateKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);

    // Certificat signé par wrongKeyPair au lieu de intermediateKeyPair
    X509Certificate endEntityCert = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=Intermediate CA", // DN correct mais...
        endEntityKeyPair.getPublic(),
        wrongKeyPair.getPublic(), // ...signature avec mauvaise clé
        wrongKeyPair.getPrivate(),
        false);

    CertificateVerifSpec spec = new CertificateVerifSpec();
    RootCertificates rootCerts = new RootCertificates(List.of(rootCert));
    IntermediateCertificates intermediateCerts = new IntermediateCertificates(
        new ArrayList<>(List.of(intermediateCert)));

    // Act
    Boolean result = chainCertificationVerification.verifyCertificateChain(
        spec,
        rootCerts,
        intermediateCerts,
        endEntityCert);

    // Assert
    assertFalse(result, "Une chaîne avec mauvaise signature devrait être invalide");
  }

  @Test
  void testInvalidChainWrongRoot() throws Exception {
    // Intermédiaire signé par wrongKeyPair au lieu de rootKeyPair
    X509Certificate intermediateCert = X509CertificateCreator.createCertificate(
        "CN=Intermediate CA",
        "CN=Root CA", // DN correct mais...
        intermediateKeyPair.getPublic(),
        wrongKeyPair.getPublic(), // ...signature avec mauvaise clé
        wrongKeyPair.getPrivate(),
        true);

    X509Certificate endEntityCert = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=Intermediate CA",
        endEntityKeyPair.getPublic(),
        intermediateKeyPair.getPublic(),
        intermediateKeyPair.getPrivate(),
        false);

    CertificateVerifSpec spec = new CertificateVerifSpec();
    RootCertificates rootCerts = new RootCertificates(List.of(rootCert));
    IntermediateCertificates intermediateCerts = new IntermediateCertificates(
        new ArrayList<>(List.of(intermediateCert)));

    // Act
    Boolean result = chainCertificationVerification.verifyCertificateChain(
        spec,
        rootCerts,
        intermediateCerts,
        endEntityCert);

    // Assert
    assertFalse(result,
        "Une chaîne dont l'intermédiaire n'est pas signé par le root devrait être invalide");
  }

  @Test
  void testInvalidChainMissingRoot() throws Exception {
    X509Certificate wrongRootCert = X509CertificateCreator.createCertificate(
        "CN=Wrong Root CA",
        "CN=Wrong Root CA",
        wrongKeyPair.getPublic(),
        wrongKeyPair.getPublic(),
        wrongKeyPair.getPrivate(),
        true);

    X509Certificate intermediateCert = X509CertificateCreator.createCertificate(
        "CN=Intermediate CA",
        "CN=Actual Root CA",
        intermediateKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);

    X509Certificate endEntityCert = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=Intermediate CA",
        endEntityKeyPair.getPublic(),
        intermediateKeyPair.getPublic(),
        intermediateKeyPair.getPrivate(),
        false);

    CertificateVerifSpec spec = new CertificateVerifSpec();
    // Trust anchors contient le mauvais root
    RootCertificates rootCerts = new RootCertificates(List.of(wrongRootCert));
    IntermediateCertificates intermediateCerts = new IntermediateCertificates(
        new ArrayList<>(List.of(intermediateCert)));

    // Act
    Boolean result = chainCertificationVerification.verifyCertificateChain(
        spec,
        rootCerts,
        intermediateCerts,
        endEntityCert);

    // Assert
    assertFalse(result, "Une chaîne sans le bon root dans les trust anchors devrait être invalide");
  }

  @Test
  void testInvalidSelfSignedCertificate() throws Exception {
    // Arrange - Certificat auto-signé qui n'est pas un trust anchor
    X509Certificate selfSignedCert = X509CertificateCreator.createCertificate(
        "CN=Self Signed",
        "CN=Self Signed",
        endEntityKeyPair.getPublic(),
        endEntityKeyPair.getPublic(),
        endEntityKeyPair.getPrivate(),
        true);

    CertificateVerifSpec spec = new CertificateVerifSpec();
    RootCertificates rootCerts = new RootCertificates(List.of(rootCert));
    IntermediateCertificates intermediateCerts = new IntermediateCertificates(new ArrayList<>());

    // Act
    Boolean result = chainCertificationVerification.verifyCertificateChain(
        spec,
        rootCerts,
        intermediateCerts,
        selfSignedCert);

    // Assert
    assertFalse(result,
        "Un certificat auto-signé non dans les trust anchors devrait être invalide");
  }

  @Test
  void testInvalidChainMismatchedIssuerDN() throws Exception {
    // Arrange
    X509Certificate intermediateCert = X509CertificateCreator.createCertificate(
        "CN=Intermediate CA",
        "CN=Root CA",
        intermediateKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);

    // End entity avec un issuer DN qui ne correspond pas
    X509Certificate endEntityCert = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=Wrong Issuer", // DN incorrect
        endEntityKeyPair.getPublic(),
        intermediateKeyPair.getPublic(),
        intermediateKeyPair.getPrivate(),
        false);

    CertificateVerifSpec spec = new CertificateVerifSpec();
    RootCertificates rootCerts = new RootCertificates(List.of(rootCert));
    IntermediateCertificates intermediateCerts = new IntermediateCertificates(
        new ArrayList<>(List.of(intermediateCert)));

    // Act
    Boolean result = chainCertificationVerification.verifyCertificateChain(
        spec,
        rootCerts,
        intermediateCerts,
        endEntityCert);

    // Assert
    assertFalse(result, "Une chaîne avec issuer DN incorrect devrait être invalide");
  }

  @Test
  void testExpiredCertificateInChain() throws Exception {
    // Arrange - Chaîne avec certificat expiré
    X509Certificate intermediateCert = X509CertificateCreator.createCertificate(
        "CN=Intermediate CA",
        "CN=Root CA",
        intermediateKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);

    X509Certificate expiredCert = X509CertificateCreator.createExpiredCertificate(
        "CN=example.com",
        "CN=Intermediate CA",
        endEntityKeyPair.getPublic(),
        intermediateKeyPair.getPublic(),
        intermediateKeyPair.getPrivate(),
        false);

    CertificateVerifSpec spec = new CertificateVerifSpec();
    RootCertificates rootCerts = new RootCertificates(List.of(rootCert));
    IntermediateCertificates intermediateCerts = new IntermediateCertificates(
        new ArrayList<>(List.of(intermediateCert)));

    // Act
    Boolean result = chainCertificationVerification.verifyCertificateChain(
        spec,
        rootCerts,
        intermediateCerts,
        expiredCert);

    // Assert
    assertFalse(result, "Un certificat expiré devrait échouer la validation");
  }

  @Test
  void testNotYetValidCertificateInChain() throws Exception {
    X509Certificate intermediateCert = X509CertificateCreator.createCertificate(
        "CN=Intermediate CA",
        "CN=Root CA",
        intermediateKeyPair.getPublic(),
        rootKeyPair.getPublic(),
        rootKeyPair.getPrivate(),
        true);

    X509Certificate notYetValidCert = X509CertificateCreator.createNotYetValidCertificate(
        "CN=example.com",
        "CN=Intermediate CA",
        endEntityKeyPair.getPublic(),
        intermediateKeyPair.getPublic(),
        intermediateKeyPair.getPrivate(),
        false);

    CertificateVerifSpec spec = new CertificateVerifSpec();
    RootCertificates rootCerts = new RootCertificates(List.of(rootCert));
    IntermediateCertificates intermediateCerts = new IntermediateCertificates(
        new ArrayList<>(List.of(intermediateCert)));

    // Act
    Boolean result = chainCertificationVerification.verifyCertificateChain(
        spec,
        rootCerts,
        intermediateCerts,
        notYetValidCert);

    // Assert
    assertFalse(result, "Un certificat pas encore valide devrait échouer la validation");
  }
}
