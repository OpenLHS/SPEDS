package ca.griis.speds.toolkit.unit.security.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.griis.speds.toolkit.security.api.DefaultCertificateFactory;
import ca.griis.speds.toolkit.unit.utilities.X509CertificateCreator;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import org.apache.jena.iri.IRIFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DefaultCertificateTest {
  @BeforeAll
  public static void setupAll() {
    Security.addProvider(new BouncyCastleProvider());
  }

  @Test
  void validateCertificateValidIri() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificate(
        "CN=example.ca",
        "CN=example.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "example.ca");

    final var iri = IRIFactory.iriImplementation().create("https://example.ca");
    final var certificateService = new DefaultCertificateFactory().init();
    final var result = certificateService.checkCertificateValidity(cert, iri);
    assertTrue(result);
  }

  @Test
  void validateCertificateInvalidIri() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificate(
        "CN=example.ca",
        "CN=example.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "example.ca");

    final var iri = IRIFactory.iriImplementation().create("https://example.com");
    final var certificateService = new DefaultCertificateFactory().init();
    final var result = certificateService.checkCertificateValidity(cert, iri);
    assertFalse(result);
  }
}
