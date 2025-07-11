package ca.griis.speds.link.unit.api.sync;

import static org.junit.jupiter.api.Assertions.*;

import ca.griis.cryptography.asymmetric.generator.RsaKeysGenerator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.Base64;
import java.util.Date;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.bouncycastle.asn1.x500.X500Name;
import org.testcontainers.shaded.org.bouncycastle.cert.X509v3CertificateBuilder;
import org.testcontainers.shaded.org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.testcontainers.shaded.org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.testcontainers.shaded.org.bouncycastle.operator.ContentSigner;
import org.testcontainers.shaded.org.bouncycastle.operator.OperatorCreationException;
import org.testcontainers.shaded.org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;


/// MD - J'ai fait un test parce que j'ai gosser un boute avec le self-sign
/// Peut-être mettre ça dans lib de crypto? test-util?
public class SelfSignedCertificateTest {

  private X509Certificate generateCertificate() {
    // 📜 Certificat X.509
    RsaKeysGenerator generator = new RsaKeysGenerator(2048);
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
  public void testCertificateEncodingDecoding() throws CertificateEncodingException {
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
