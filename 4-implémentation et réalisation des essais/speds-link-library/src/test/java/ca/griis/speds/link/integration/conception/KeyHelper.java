package ca.griis.speds.link.integration.conception;

import ca.griis.cryptography.asymmetric.generator.RsaKeysGenerator;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testcontainers.shaded.org.bouncycastle.asn1.x500.X500Name;
import org.testcontainers.shaded.org.bouncycastle.cert.X509v3CertificateBuilder;
import org.testcontainers.shaded.org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.testcontainers.shaded.org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.testcontainers.shaded.org.bouncycastle.operator.ContentSigner;
import org.testcontainers.shaded.org.bouncycastle.operator.OperatorCreationException;
import org.testcontainers.shaded.org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class KeyHelper {

  public static Map.Entry<String, String> makeKeyWithCert() {
    try {
      // 📜 Certificat X.509
      RsaKeysGenerator generator = new RsaKeysGenerator(2048);
      KeyPair keyPair = generator.generateKeyPair();
      X509Certificate cert = generateSelfSignedCertificate(keyPair);
      String serialCert = Base64.getEncoder().encodeToString(cert.getEncoded());

      // 🔑 Clef privée de chiffrement
      String serialKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

      return Map.entry(serialKey, serialCert);
    } catch (CertificateEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) {
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
}
