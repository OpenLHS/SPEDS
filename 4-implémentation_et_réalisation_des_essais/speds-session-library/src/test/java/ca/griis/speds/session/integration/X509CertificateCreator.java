package ca.griis.speds.session.integration;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.NamedParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class X509CertificateCreator {
  // ---------------------------------------------------------
  // Fonction utilitaire : génération d'un certificat X.509
  // ---------------------------------------------------------
  public static X509Certificate createCertificate(
      String subjectDN,
      String issuerDN,
      PublicKey subjectKey,
      PublicKey issuerPublicKey,
      PrivateKey issuerPrivateKey,
      Boolean isCA,
      Boolean expired,
      String dnsName,
      String algo) throws Exception {

    long now = System.currentTimeMillis();
    Date notBefore = new Date(now - 86400000);
    Date notAfter = new Date(now + 3650L * 24 * 3600 * 1000);

    if (expired) {
      notBefore = new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000);
      notAfter = new Date(System.currentTimeMillis() - 24 * 3600 * 1000);
    }

    X500Name issuer = new X500Name(issuerDN);
    X500Name subject = new X500Name(subjectDN);

    JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
        issuer,
        BigInteger.valueOf(now),
        notBefore,
        notAfter,
        subject,
        subjectKey);

    builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isCA));
    builder.addExtension(Extension.keyUsage, true,
        new KeyUsage(isCA ? KeyUsage.keyCertSign | KeyUsage.cRLSign
            : KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

    if (dnsName.isBlank() == false) {
      final GeneralName dns = new GeneralName(GeneralName.dNSName, dnsName);
      final GeneralNames subjectAltNames = new GeneralNames(dns);
      builder.addExtension(Extension.subjectAlternativeName, true, subjectAltNames);
    }

    builder.addExtension(Extension.subjectKeyIdentifier, false, createSKI(subjectKey));
    builder.addExtension(Extension.authorityKeyIdentifier, false, createAKI(issuerPublicKey));

    ContentSigner signer = new JcaContentSignerBuilder(algo).build(issuerPrivateKey);

    return new JcaX509CertificateConverter()
        .setProvider("BC")
        .getCertificate(builder.build(signer));
  }

  // ---------------------------------------------------------
  // Génération d'une paire de clés RSA
  // ---------------------------------------------------------
  public static KeyPair generateRsaKeyPair(Integer keyLengthBits) throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(new RSAKeyGenParameterSpec(keyLengthBits, RSAKeyGenParameterSpec.F4));
    return kpg.generateKeyPair();
  }

  // ---------------------------------------------------------
  // Génération d'une paire de clés RSA
  // ---------------------------------------------------------
  public static KeyPair generateEdKeyPair() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
    kpg.initialize(NamedParameterSpec.ED25519);
    return kpg.generateKeyPair();
  }

  // ---------------------------------------------------------
  // SKI / AKI
  // ---------------------------------------------------------
  private static SubjectKeyIdentifier createSKI(PublicKey key) throws Exception {
    return new JcaX509ExtensionUtils().createSubjectKeyIdentifier(key);
  }

  private static AuthorityKeyIdentifier createAKI(PublicKey issuerPublicKey) throws Exception {
    return new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(issuerPublicKey);
  }
}
