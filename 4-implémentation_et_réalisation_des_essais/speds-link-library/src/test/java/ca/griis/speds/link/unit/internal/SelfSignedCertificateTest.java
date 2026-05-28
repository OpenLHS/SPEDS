package ca.griis.speds.link.unit.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Test;

public class SelfSignedCertificateTest {
  private X509Certificate generateCertificate() throws NoSuchAlgorithmException {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair keyPair = generator.generateKeyPair();

    Security.addProvider(new BouncyCastleProvider());

    long now = System.currentTimeMillis();
    Date startDate = new Date(now - 1000L * 60 * 60); // 1h avant
    Date endDate = new Date(now + 1000L * 60 * 60 * 24 * 365); // 1 an de validité

    X500Name dnName = new X500Name("CN=Self-Signed Certificate");
    BigInteger certSerialNumber = BigInteger.valueOf(now);

    X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
        dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());

    ContentSigner contentSigner;
    try {
      contentSigner = new JcaContentSignerBuilder("SHA256WithRSA")
          .setProvider("BC")
          .build(keyPair.getPrivate());
    } catch (OperatorCreationException e) {
      throw new RuntimeException(e);
    }

    try {
      X509Certificate cert = new JcaX509CertificateConverter()
          .setProvider("BC")
          .getCertificate(certBuilder.build(contentSigner));
      return cert;
    } catch (CertificateException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testCertificateEncodingDecoding()
      throws CertificateEncodingException, NoSuchAlgorithmException {
    // given
    // Générer un certificat auto-signé
    X509Certificate x509Certificate = generateCertificate();
    String base64Certificate = Base64.getEncoder().encodeToString(x509Certificate.getEncoded());

    // when
    // Tenter de le décoder et de le reconstruire
    try {
      byte[] certificateBytes = Base64.getDecoder().decode(base64Certificate);
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(certificateBytes)) {
        X509Certificate decodedCertificate =
            (X509Certificate) certificateFactory.generateCertificate(inputStream);

        // then
        // Vérifier si le certificat reconstruit est valide
        assertNotNull(decodedCertificate);
      }
    } catch (IOException | CertificateException | IllegalArgumentException e) {
      fail("Invalid Base64 encoded Certificate: " + e.getMessage());
    }
  }
}
