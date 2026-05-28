package ca.griis.security.unit.internal.certificate.verification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.security.api.domain.spec.certificate.Hostname;
import ca.griis.security.api.domain.spec.certificate.usage.CertificateKeyUsages;
import ca.griis.security.api.domain.spec.certificate.usage.KeyUsageType;
import ca.griis.security.api.service.DefaultSecurityService;
import ca.griis.security.unit.util.X509CertificateCreator;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CertificateExtensionsVerificationTest {
  private DefaultSecurityService verification;
  private KeyPair keyPair;

  @BeforeEach
  void setUp() throws Exception {
    verification = new DefaultSecurityService();
    keyPair = X509CertificateCreator.generateKeyPair();
  }

  @Test
  public void testBasicConstraintsValidNonCA() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false // isCA = false → basicConstraints = -1
    );

    Hostname hostname = new Hostname("example.com");

    // Act
    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        null);

    // Assert
    assertTrue(result, "Certificat non-CA devrait être valide");
  }

  @Test
  void testBasicConstraintsInvalidCA() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=CA Certificate",
        "CN=CA Certificate",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        true);

    Hostname hostname = new Hostname("example.com");

    // Act
    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        null);

    assertFalse(result);
  }

  @Test
  void testKeyUsageAllPresent() throws Exception {

    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false);

    Set<KeyUsageType> requiredUsages = Set.of(
        KeyUsageType.DIGITAL_SIGNATURE,
        KeyUsageType.KEY_ENCIPHERMENT);
    CertificateKeyUsages keyUsages = new CertificateKeyUsages(requiredUsages);
    Hostname hostname = new Hostname("example.com");

    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        keyUsages);

    assertTrue(result);
  }

  @Test
  void testKeyUsageMissing() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false);

    Set<KeyUsageType> requiredUsages = Set.of(
        KeyUsageType.DIGITAL_SIGNATURE,
        KeyUsageType.KEY_CERT_SIGN);
    CertificateKeyUsages keyUsages = new CertificateKeyUsages(requiredUsages);
    Hostname hostname = new Hostname("example.com");

    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        keyUsages);

    assertFalse(result);
  }

  @Test
  void testNoKeyUsageRequiredNull() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false);

    Hostname hostname = new Hostname("example.com");

    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        null);

    assertTrue(result);
  }

  @Test
  void testEmptyKeyUsageSet() throws Exception {

    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false);

    CertificateKeyUsages keyUsages = new CertificateKeyUsages(Set.of());
    Hostname hostname = new Hostname("example.com");


    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        keyUsages);


    assertFalse(result);
  }

  @Test
  void testSingleKeyUsage() throws Exception {

    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false);

    Set<KeyUsageType> requiredUsages = Set.of(
        KeyUsageType.DIGITAL_SIGNATURE);
    CertificateKeyUsages keyUsages = new CertificateKeyUsages(requiredUsages);
    Hostname hostname = new Hostname("example.com");

    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        keyUsages);

    assertFalse(result);
  }

  @Test
  void testMultipleKeyUsages() throws Exception {

    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=CA Certificate",
        "CN=CA Certificate",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        true);

    Set<KeyUsageType> requiredUsages = Set.of(
        KeyUsageType.KEY_CERT_SIGN,
        KeyUsageType.CRL_SIGN);
    CertificateKeyUsages keyUsages = new CertificateKeyUsages(requiredUsages);

    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        null,
        keyUsages);

    assertFalse(result);
  }

  @Test
  void testPartialKeyUsageMatch() throws Exception {

    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false);

    Set<KeyUsageType> requiredUsages = Set.of(
        KeyUsageType.DIGITAL_SIGNATURE,
        KeyUsageType.KEY_ENCIPHERMENT,
        KeyUsageType.NON_REPUDIATION);
    CertificateKeyUsages keyUsages = new CertificateKeyUsages(requiredUsages);
    Hostname hostname = new Hostname("example.com");

    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        keyUsages);

    assertTrue(result);
  }

  @Test
  void testCommonNameMatch() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false);

    Hostname hostname = new Hostname("example.com");

    // Act
    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        null);

    // Assert
    assertTrue(result);
  }

  @Test
  void testHostnameMismatch() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificate(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false);

    Hostname hostname = new Hostname("different.com");

    // Act
    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        null);

    // Assert
    assertFalse(result);
  }

  @Test
  void testSANMatch() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificateWithSAN(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false,
        false,
        "example.com", "www.example.com");

    Hostname hostname = new Hostname("example.com");

    // Act
    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        null);

    // Assert
    assertTrue(result);
  }

  @Test
  void testSANWildcardMatch() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificateWithSAN(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false,
        false,
        "*.example.com");

    Hostname hostname = new Hostname("www.example.com");

    // Act
    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        null);

    // Assert
    assertFalse(result);
  }

  @Test
  void testSANWildcardNoMatchRoot() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificateWithSAN(
        "CN=other.com",
        "CN=other.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false,
        false,
        "*.example.com");

    Hostname hostname = new Hostname("example.com");

    // Act
    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        null);

    // Assert
    assertFalse(result);
  }

  @Test
  void testFallbackToCN() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificateWithSAN(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false,
        false,
        "other.com");

    Hostname hostname = new Hostname("example.com");

    // Act
    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        null);

    // Assert
    assertTrue(result);
  }

  @Test
  void testSecondSANMatch() throws Exception {
    // Arrange
    X509Certificate certificate = X509CertificateCreator.createCertificateWithSAN(
        "CN=example.com",
        "CN=example.com",
        keyPair.getPublic(),
        keyPair.getPublic(),
        keyPair.getPrivate(),
        false,
        false,
        "other.com", "example.com", "third.com");

    Hostname hostname = new Hostname("example.com");

    // Act
    Boolean result = verification.verifyCertificateExtensions(
        certificate,
        hostname,
        null);

    // Assert
    assertTrue(result);
  }
}
