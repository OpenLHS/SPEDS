package ca.griis.speds.toolkit.integration;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SecurityProfile;
import ca.griis.security.internal.algorithm.SecretKeyGeneratorAlgorithm;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

class GlobalData {
  // Général
  public static final String projectId = UUID.randomUUID().toString();
  public static final String entity1Code = UUID.randomUUID().toString();
  public static final IRI entity1Iri =
      IRIFactory.iriImplementation().construct("https://entity1.iri");
  public static final KeyPair entity1KeyPair;

  // Sécurité
  public static final IRI someEntityIri =
      IRIFactory.iriImplementation().construct("https://some.entity.iri");
  public static final X509Certificate someEntityCertificate;

  // Cryptographie
  public static final byte[] data = "data".getBytes(StandardCharsets.UTF_8);

  public static final SecretKey strongKey;
  public static final SecretKey efficientKey;
  public static final byte[] strongSymEncryptedData;
  public static final byte[] efficientSymEncryptedData;

  public static final KeyPair strongKeyPair;
  public static final KeyPair efficientKeyPair;
  public static final byte[] strongAsymEncryptedData;
  public static final byte[] efficientAsymEncryptedData;

  public static final byte[] strongHash;
  public static final byte[] efficientHash;

  public static final byte[] strongSignature;
  public static final byte[] efficientSignature;

  public static final KeyPair choiceX;
  public static final KeyPair choiceY;
  public static final SecretKey strongSharedKey;
  public static final SecretKey efficientSharedKey;
  public static final byte[] strongSharedSymEncryptedData;
  public static final byte[] efficientSharedSymEncryptedData;

  private static SecureRandom random = new SecureRandom();

  static {
    entity1KeyPair = createKeyPair(SecurityProfile.STRONG);
    someEntityCertificate = createCertificate();

    strongKey = createSecretKey(SecurityProfile.STRONG);
    efficientKey = createSecretKey(SecurityProfile.EFFICIENT);
    strongSymEncryptedData = encryptSymmetric(strongKey);
    efficientSymEncryptedData = encryptSymmetric(efficientKey);

    strongKeyPair = createKeyPair(SecurityProfile.STRONG);
    efficientKeyPair = createKeyPair(SecurityProfile.EFFICIENT);
    strongAsymEncryptedData = encryptAsymmetric(SecurityProfile.STRONG, strongKeyPair.getPublic());
    efficientAsymEncryptedData =
        encryptAsymmetric(SecurityProfile.EFFICIENT, efficientKeyPair.getPublic());

    strongHash = createHash(SecurityProfile.STRONG);
    efficientHash = createHash(SecurityProfile.EFFICIENT);

    strongSignature = createSignature(SecurityProfile.STRONG);
    efficientSignature = createSignature(SecurityProfile.EFFICIENT);

    choiceX = createDhValue();
    choiceY = createDhValue();
    strongSharedKey = createSharedKey(SecurityProfile.STRONG);
    efficientSharedKey = createSharedKey(SecurityProfile.EFFICIENT);
    strongSharedSymEncryptedData = encryptSymmetric(strongSharedKey);
    efficientSharedSymEncryptedData = encryptSymmetric(efficientSharedKey);
  }

  private static X509Certificate createCertificate() {
    final X509Certificate certificate;

    try {
      final KeyPair authorityKeyPair = createKeyPair(SecurityProfile.STRONG);

      final Instant now = Instant.now();
      final X500Name issuer = new X500Name("CN=someIssuer");
      final X500Name subject = new X500Name("CN=" + someEntityIri.toString());
      final X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
          issuer,
          BigInteger.ONE,
          Date.from(now),
          Date.from(now.plusSeconds(60L * 60 * 24 * 365)),
          subject,
          entity1KeyPair.getPublic());
      final ContentSigner signer =
          new JcaContentSignerBuilder("SHA256withRSA")
              .build(authorityKeyPair.getPrivate());
      certificate =
          new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider())
              .getCertificate(certBuilder.build(signer));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return certificate;
  }

  private static byte[] createHash(SecurityProfile securityProfile) {
    MessageDigest messageDigest = null;
    try {
      if (securityProfile == SecurityProfile.EFFICIENT) {
        messageDigest = MessageDigest.getInstance("SHA-256");
      } else {
        messageDigest = MessageDigest.getInstance("SHA-512");
      }
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    return messageDigest.digest(data);
  }

  private static byte[] createSignature(SecurityProfile securityProfile) {
    final byte[] signature;

    try {
      Signature signatureAlgo = Signature.getInstance("RSASSA-PSS");
      PSSParameterSpec pssSpec;
      if (securityProfile == SecurityProfile.EFFICIENT) {
        signatureAlgo =
            Signature.getInstance("RSASSA-PSS");
        pssSpec = new PSSParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            32,
            1);
        signatureAlgo.setParameter(pssSpec);
        signatureAlgo.initSign(efficientKeyPair.getPrivate());
      } else {
        pssSpec = new PSSParameterSpec(
            "SHA-512",
            "MGF1",
            MGF1ParameterSpec.SHA512,
            64,
            1);
        signatureAlgo.setParameter(pssSpec);
        signatureAlgo.initSign(strongKeyPair.getPrivate());
      }

      signatureAlgo.update(data);
      signature = signatureAlgo.sign();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return signature;
  }

  private static KeyPair createKeyPair(SecurityProfile securityProfile) {
    final KeyPair kp;

    try {
      final KeyPairGenerator asymKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
      if (securityProfile == SecurityProfile.EFFICIENT) {
        final AlgorithmParameterSpec params =
            new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
        asymKeyPairGenerator.initialize(params);
      } else {
        final AlgorithmParameterSpec params =
            new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4);
        asymKeyPairGenerator.initialize(params);
      }

      kp = asymKeyPairGenerator.generateKeyPair();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return kp;
  }

  private static byte[] encryptAsymmetric(SecurityProfile securityProfile, PublicKey publicKey) {
    final byte[] encrypted;

    try {
      final Cipher rsaCipherAlgo;
      final String mdName;
      if (securityProfile == SecurityProfile.EFFICIENT) {
        rsaCipherAlgo = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        mdName = "SHA-256";
      } else {
        rsaCipherAlgo = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        mdName = "SHA-512";
      }
      final OAEPParameterSpec oaepParams =
          new OAEPParameterSpec(mdName, "MGF1", new MGF1ParameterSpec(mdName),
              PSource.PSpecified.DEFAULT);
      rsaCipherAlgo.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);
      encrypted = rsaCipherAlgo.doFinal(data);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return encrypted;
  }

  private static SecretKey createSecretKey(SecurityProfile securityProfile) {
    final SecretKey sk;

    try {
      final KeyGenerator symKeyGenerator;
      symKeyGenerator = KeyGenerator.getInstance(SecretKeyGeneratorAlgorithm.AES.getAlgorithm());
      if (securityProfile == SecurityProfile.EFFICIENT) {
        symKeyGenerator.init(128);
      } else {
        symKeyGenerator.init(256);
      }

      sk = symKeyGenerator.generateKey();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return sk;
  }

  private static byte[] encryptSymmetric(SecretKey symmetricKey) {
    final byte[] encrypted;

    try {
      byte[] iv = new byte[96 / 8];
      random.nextBytes(iv);
      final GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

      final Cipher aesCipherAlgo = Cipher.getInstance("AES/GCM/NoPadding");
      aesCipherAlgo.init(Cipher.ENCRYPT_MODE, symmetricKey, parameterSpec);
      final byte[] symEncrypted = aesCipherAlgo.doFinal(data);
      encrypted =
          ByteBuffer.allocate(iv.length + symEncrypted.length).put(iv).put(symEncrypted).array();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return encrypted;
  }

  private static KeyPair createDhValue() {
    final KeyPair value;
    try {
      final KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519");
      value = kpg.generateKeyPair();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return value;
  }

  private static SecretKey createSharedKey(SecurityProfile securityProfile) {
    final SecretKey shared;

    try {
      final KeyAgreement keyAgreement = KeyAgreement.getInstance("X25519");
      keyAgreement.init(choiceX.getPrivate());
      keyAgreement.doPhase(choiceY.getPublic(), true);
      final byte[] sharedByte = keyAgreement.generateSecret();

      final byte[] salt = new byte[32];
      final SecureRandom sr = SecureRandom.getInstanceStrong();
      sr.nextBytes(salt);

      final byte[] info = "speds-toolkit-test".getBytes(StandardCharsets.UTF_8);

      final HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
      hkdf.init(new HKDFParameters(sharedByte, salt, info));

      final byte[] okm;
      if (securityProfile == SecurityProfile.EFFICIENT) {
        okm = new byte[16];
      } else {
        okm = new byte[32];
      }
      hkdf.generateBytes(okm, 0, okm.length);

      shared = new SecretKeySpec(okm, "AES");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return shared;
  }
}
