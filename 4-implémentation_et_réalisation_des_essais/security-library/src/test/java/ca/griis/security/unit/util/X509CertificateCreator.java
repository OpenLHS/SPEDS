package ca.griis.security.unit.util;

import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class X509CertificateCreator {
  // ---------------------------------------------------------
  // Fonction utilitaire : génération d'un certificat X.509
  // ---------------------------------------------------------
  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  public static URI writeCRLToTempFile(X509CRL crl) throws Exception {
    Path tempFile = Files.createTempFile("test-ca", ".crl");
    Files.write(tempFile, crl.getEncoded());
    return tempFile.toUri();
  }

  public static X509CRL createCRL(
      String issuerDN,
      PrivateKey caPrivateKey,
      X509Certificate caCert,
      X509Certificate revokedCert) throws Exception {
    Long now = System.currentTimeMillis();
    X500Name issuer = new X500Name(issuerDN);
    X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(issuer, new Date(now));

    if (revokedCert != null) {
      crlBuilder.addCRLEntry(revokedCert.getSerialNumber(), new Date(now), CRLReason.keyCompromise);
    }

    crlBuilder.setNextUpdate(new Date(now + 24 * 3600 * 1000));

    ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
        .build(caPrivateKey);

    return new JcaX509CRLConverter()
        .setProvider("BC")
        .getCRL(crlBuilder.build(signer));
  }

  public static X509Certificate createCertificate(
      String subjectDN,
      String issuerDN,
      PublicKey subjectKey,
      PublicKey issuerPublicKey,
      PrivateKey issuerPrivateKey,
      Boolean isCA) throws Exception {

    final var now = System.currentTimeMillis();
    Date notBefore = new Date(now - 86400000);
    Date notAfter = new Date(now + 3650L * 24 * 3600 * 1000);

    X500Name issuer = new X500Name(issuerDN); // Utilise issuerDN
    X500Name subject = new X500Name(subjectDN); // Utilise subjectDN

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

    builder.addExtension(Extension.subjectKeyIdentifier, false, createSKI(subjectKey));
    builder.addExtension(Extension.authorityKeyIdentifier, false, createAKI(issuerPublicKey));

    ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(issuerPrivateKey);
    return new JcaX509CertificateConverter()
        .setProvider("BC")
        .getCertificate(builder.build(signer));
  }

  public static X509Certificate createCertificateWithSAN(
      String subjectDN,
      String issuerDN,
      PublicKey subjectKey,
      PublicKey issuerPublicKey,
      PrivateKey issuerPrivateKey,
      Boolean isCA,
      Boolean expired,
      String... sanDNSNames) throws Exception {

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

    // Ajouter le SAN
    if (sanDNSNames != null && sanDNSNames.length > 0) {
      GeneralName[] names = new GeneralName[sanDNSNames.length];
      for (int i = 0; i < sanDNSNames.length; i++) {
        names[i] = new GeneralName(GeneralName.dNSName, sanDNSNames[i]);
      }
      GeneralNames subjectAltNames = new GeneralNames(names);
      builder.addExtension(Extension.subjectAlternativeName, true, subjectAltNames);
    }

    builder.addExtension(Extension.subjectKeyIdentifier, false, createSKI(subjectKey));
    builder.addExtension(Extension.authorityKeyIdentifier, false, createAKI(issuerPublicKey));

    ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(issuerPrivateKey);

    return new JcaX509CertificateConverter()
        .setProvider("BC")
        .getCertificate(builder.build(signer));
  }

  public static X509Certificate createExpiredCertificate(
      String subjectDN,
      String issuerDN,
      PublicKey subjectKey,
      PublicKey issuerPublicKey,
      PrivateKey issuerPrivateKey,
      Boolean isCA) throws Exception {

    long now = System.currentTimeMillis();
    Date notBefore = new Date(now - 365L * 24 * 3600 * 1000); // Il y a 1 an
    Date notAfter = new Date(now - 86400000); // Hier

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

    builder.addExtension(Extension.subjectKeyIdentifier, false, createSKI(subjectKey));
    builder.addExtension(Extension.authorityKeyIdentifier, false, createAKI(issuerPublicKey));

    ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(issuerPrivateKey);

    return new JcaX509CertificateConverter()
        .setProvider("BC")
        .getCertificate(builder.build(signer));
  }

  // Certificat pas encore valide
  public static X509Certificate createNotYetValidCertificate(
      String subjectDN,
      String issuerDN,
      PublicKey subjectKey,
      PublicKey issuerPublicKey,
      PrivateKey issuerPrivateKey,
      Boolean isCA) throws Exception {

    long now = System.currentTimeMillis();
    Date notBefore = new Date(now + 86400000); // Demain
    Date notAfter = new Date(now + 365L * 24 * 3600 * 1000); // Dans 1 an

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

    builder.addExtension(Extension.subjectKeyIdentifier, false, createSKI(subjectKey));
    builder.addExtension(Extension.authorityKeyIdentifier, false, createAKI(issuerPublicKey));

    ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(issuerPrivateKey);

    return new JcaX509CertificateConverter()
        .setProvider("BC")
        .getCertificate(builder.build(signer));
  }

  // ---------------------------------------------------------
  // Génération d'une paire de clés RSA
  // ---------------------------------------------------------
  public static KeyPair generateKeyPair() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
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
