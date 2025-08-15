package ca.griis.speds.session.integration.concurency;

import ca.griis.cryptography.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.cryptography.symmetric.generator.SecretKeyGenerator;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class KeyUtil {

  static {
    // Ajouter le provider Bouncy Castle
    Security.addProvider(new BouncyCastleProvider());
  }

  public KeyPair generateRSAKeyPair() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
    generator.initialize(4096);
    return generator.generateKeyPair();
  }

  public X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
    X500Principal subject = new X500Principal("CN=Test,O=Test,C=CA");
    BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
    Date notBefore = new Date();
    Date notAfter = new Date(notBefore.getTime() + (365L * 24 * 60 * 60 * 1000)); // 1 an

    X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
        subject, serial, notBefore, notAfter, subject, keyPair.getPublic());

    ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
        .setProvider("BC")
        .build(keyPair.getPrivate());

    X509CertificateHolder certHolder = certBuilder.build(signer);
    return new JcaX509CertificateConverter()
        .setProvider("BC")
        .getCertificate(certHolder);
  }

  public SecretKey generateSdek() {
    return SecretKeyGenerator.generateSymmetricKey(SecretKeyGeneratorAlgorithm.AES, 256);
  }

  public String certificateToString(X509Certificate cert) throws Exception {
    return Base64.getEncoder().encodeToString(cert.getEncoded());
  }

  public String privateKeyToString(PrivateKey key) {
    return Base64.getEncoder().encodeToString(key.getEncoded());
  }

  public String publicKeyToString(PublicKey key) {
    return Base64.getEncoder().encodeToString(key.getEncoded());
  }

  public String secretKeyToString(SecretKey key) {
    return Base64.getEncoder().encodeToString(key.getEncoded());
  }
}
