/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe DefaultSecurityService.
 * @brief @~english Implementation of the class DefaultSecurityService.
 */

package ca.griis.security.api.service;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.security.api.SecurityService;
import ca.griis.security.api.domain.Digest;
import ca.griis.security.api.domain.DigitalSignature;
import ca.griis.security.api.domain.SecurityProfile;
import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.security.api.domain.spec.certificate.CertificateVerifSpec;
import ca.griis.security.api.domain.spec.certificate.Hostname;
import ca.griis.security.api.domain.spec.certificate.IntermediateCertificates;
import ca.griis.security.api.domain.spec.certificate.RootCertificates;
import ca.griis.security.api.domain.spec.certificate.usage.CertificateKeyUsages;
import ca.griis.security.api.domain.spec.cipher.asym.AsymCipherSpec;
import ca.griis.security.api.domain.spec.cipher.asym.RsaCipherSpec;
import ca.griis.security.api.domain.spec.cipher.symm.SymCipherSpec;
import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.domain.spec.dh.KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.generator.asym.AsymKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.RsaKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.SymKeyGenSpec;
import ca.griis.security.api.domain.spec.hash.HashFnSpec;
import ca.griis.security.api.domain.spec.sign.RsaSignatureFnSpec;
import ca.griis.security.api.domain.spec.sign.SignatureFnSpec;
import ca.griis.security.internal.algorithm.CipherAlgorithm;
import ca.griis.security.internal.algorithm.KeyAgreementAlgorithm;
import ca.griis.security.internal.algorithm.MessageDigestAlgorithm;
import ca.griis.security.internal.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.security.internal.algorithm.SignatureAlgorithm;
import ca.griis.security.internal.asymmetric.encryption.RsaDecryptor;
import ca.griis.security.internal.asymmetric.encryption.RsaEncryptor;
import ca.griis.security.internal.asymmetric.generator.AsymKeysGenerator;
import ca.griis.security.internal.asymmetric.generator.Ed25519KeysGenerator;
import ca.griis.security.internal.asymmetric.generator.RsaKeysGenerator;
import ca.griis.security.internal.asymmetric.generator.X25519KeysGenerator;
import ca.griis.security.internal.certificate.verification.CertificateExtensionsVerification;
import ca.griis.security.internal.certificate.verification.ChainCertificationVerification;
import ca.griis.security.internal.certificate.verification.SignatureVerification;
import ca.griis.security.internal.certificate.verification.TemporalVerification;
import ca.griis.security.internal.hash.hashing.Hashing;
import ca.griis.security.internal.hash.hashing.Sha256Hashing;
import ca.griis.security.internal.hash.hashing.Sha512Hashing;
import ca.griis.security.internal.hash.verification.Sha256VerifyHashing;
import ca.griis.security.internal.hash.verification.Sha512VerifyHashing;
import ca.griis.security.internal.hash.verification.VerifyHashing;
import ca.griis.security.internal.keyexchange.KeyAgreementProvider;
import ca.griis.security.internal.keyexchange.X25519Provider;
import ca.griis.security.internal.signature.signing.EdDsaSigning;
import ca.griis.security.internal.signature.signing.RsaSigning;
import ca.griis.security.internal.signature.signing.Signing;
import ca.griis.security.internal.signature.verification.EdDsaVerifySigning;
import ca.griis.security.internal.signature.verification.RsaVerifySigning;
import ca.griis.security.internal.signature.verification.VerifySigning;
import ca.griis.security.internal.spec.SpecProvider;
import ca.griis.security.internal.symmetric.encryption.AesGcmDecryptor;
import ca.griis.security.internal.symmetric.encryption.AesGcmEncryptor;
import ca.griis.security.internal.symmetric.generator.SecretKeyGenerator;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details
 *      «Detailed description of the component (optional)»
 * @par Model
 *      «Model (Abstract, automation, etc.) (optional)»
 * @par Conception
 *      «Conception description (criteria and constraints) (optional)»
 * @par Limits
 *      «Limits description (optional)»
 *
 * @brief @~french Définit le service de sécurité.
 * @par Details
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      «2025-12-17» [BD] - Implémentation initiale
 * @par Tâches
 *      S.O.
 */
public class DefaultSecurityService implements SecurityService {
  private static final GriisLogger logger = getLogger(DefaultSecurityService.class);

  private final List<X509CRL> crl;

  public DefaultSecurityService(List<X509CRL> crl) {
    this.crl = crl;
  }

  public DefaultSecurityService() {
    this.crl = List.of();
  }

  @Override
  public Map<String, SecuritySpec> getProfilSecuritySpecs(SecurityProfile securityProfile) {
    logger.trace(Trace.ENTER_METHOD_1, "securityProfile", securityProfile);

    SpecProvider specProvider = new SpecProvider();
    Map<String, SecuritySpec> securityProfileSpecs =
        specProvider.getProfilSecuritySpecs(securityProfile);

    logger.trace(Trace.EXIT_METHOD_1, "securityProfileSpecs", securityProfileSpecs);
    return securityProfileSpecs;
  }

  @Override
  public Digest generateDigest(HashFnSpec hashFnSpec, byte[] data) {
    logger.trace(Trace.ENTER_METHOD_2, "hashFnSpec", hashFnSpec, "data",
        System.identityHashCode(data));

    Hashing hashing;
    if (hashFnSpec.getAlgo().equals(MessageDigestAlgorithm.SHA512.getAlgorithm())) {
      hashing = new Sha512Hashing();
    } else if (hashFnSpec.getAlgo().equals(MessageDigestAlgorithm.SHA256.getAlgorithm())) {
      hashing = new Sha256Hashing();
    } else {
      throw new IllegalArgumentException("Unexpected hashFnSpec value !");
    }

    Digest digest = hashing.hash(data);

    logger.trace(Trace.EXIT_METHOD_1, "digest", System.identityHashCode(digest));
    return digest;
  }

  @Override
  public Boolean verifyDigest(HashFnSpec hashFnSpec, Digest digest, byte[] data) {
    logger.trace(Trace.ENTER_METHOD_3, "hashFnSpec", hashFnSpec, "digest",
        System.identityHashCode(digest), "data", System.identityHashCode(data));

    VerifyHashing verifyHashing;
    if (hashFnSpec.getAlgo().equals(MessageDigestAlgorithm.SHA512.getAlgorithm())) {
      verifyHashing = new Sha512VerifyHashing();
    } else if (hashFnSpec.getAlgo().equals(MessageDigestAlgorithm.SHA256.getAlgorithm())) {
      verifyHashing = new Sha256VerifyHashing();
    } else {
      throw new IllegalArgumentException("Unexpected hashFnSpec value !");
    }

    Boolean result = verifyHashing.verify(data, digest);

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  @Override
  public SecretKey generateSecretKey(CsprngSpec csprngSpec, SymKeyGenSpec symKeyGenSpec) {
    logger.trace(Trace.ENTER_METHOD_2, "csprngSpec", csprngSpec, "symKeyGenSpec", symKeyGenSpec);

    Integer keyBitLength = symKeyGenSpec.getKeyBitLength();
    SecretKey key = SecretKeyGenerator.generateSymmetricKey(symKeyGenSpec.getAlgo(),
        keyBitLength, csprngSpec);

    logger.trace(Trace.EXIT_METHOD_1, "key", System.identityHashCode(key));
    return key;
  }

  @Override
  public byte[] symEncrypt(CsprngSpec csprngSpec, SymCipherSpec symCipherSpec, SecretKey secretKey,
      byte[] data) {
    logger.trace(Trace.ENTER_METHOD_4, "csprngSpec", csprngSpec, "symCipherSpec", symCipherSpec,
        "secretKey", System.identityHashCode(secretKey), "data", System.identityHashCode(data));

    if (!(symCipherSpec.getAlgo().equals(CipherAlgorithm.AESGCM.getAlgorithm()))) {
      throw new IllegalArgumentException("Unexpected symCipherSpec value !");
    }

    AesGcmEncryptor encryptor = new AesGcmEncryptor(secretKey);
    byte[] encryptedData = encryptor.encrypt(data, csprngSpec);

    logger.trace(Trace.EXIT_METHOD_1, "encryptedData", System.identityHashCode(encryptedData));
    return encryptedData;
  }

  @Override
  public byte[] symDecrypt(CsprngSpec csprngSpec, SymCipherSpec symCipherSpec, SecretKey secretKey,
      byte[] encryptedData) {
    logger.trace(Trace.ENTER_METHOD_4, "csprngSpec", csprngSpec, "symCipherSpec", symCipherSpec,
        "secretKey", System.identityHashCode(secretKey), "encryptedData",
        System.identityHashCode(encryptedData));

    if (!(symCipherSpec.getAlgo().equals(CipherAlgorithm.AESGCM.getAlgorithm()))) {
      throw new IllegalArgumentException("Unexpected symCipherSpec value !");
    }

    AesGcmDecryptor decryptor = new AesGcmDecryptor(secretKey);
    byte[] decryptedData = decryptor.decrypt(encryptedData, csprngSpec);

    logger.trace(Trace.EXIT_METHOD_1, "decryptedData", System.identityHashCode(decryptedData));
    return decryptedData;
  }

  @Override
  public KeyPair generateKeyPair(CsprngSpec csprngSpec, AsymKeyPairGenSpec asymKeyPairGenSpec) {
    logger.trace(Trace.ENTER_METHOD_2, "csprngSpec", csprngSpec, "asymKeyPairGenSpec",
        asymKeyPairGenSpec);

    AsymKeysGenerator generator;
    if (asymKeyPairGenSpec.getAlgo().equals("RSA")) {
      generator = new RsaKeysGenerator(
          ((RsaKeyPairGenSpec) asymKeyPairGenSpec).getKeyGenParameterSpec().getKeysize());
    } else if (asymKeyPairGenSpec.getAlgo().equals("Ed25519")) {
      generator = new Ed25519KeysGenerator();
    } else if (asymKeyPairGenSpec.getAlgo().equals("X25519")) {
      generator = new X25519KeysGenerator();
    } else {
      throw new IllegalArgumentException("Unexpected asymKeyPairGenSpec value !");
    }

    KeyPair keyPair = generator.generateKeyPair(csprngSpec);

    logger.trace(Trace.EXIT_METHOD_1, "keyPair", System.identityHashCode(keyPair));
    return keyPair;
  }

  @Override
  public byte[] asymEncrypt(CsprngSpec csprngSpec, AsymCipherSpec asymCipherSpec,
      PublicKey publicKey, byte[] data) {
    logger.trace(Trace.ENTER_METHOD_4, "csprngSpec", csprngSpec, "asymCipherSpec", asymCipherSpec,
        "publicKey", System.identityHashCode(publicKey), "data", System.identityHashCode(data));

    RsaEncryptor encryptor;

    if (asymCipherSpec.getAlgo().equals(CipherAlgorithm.RSA.getAlgorithm())) {
      encryptor = new RsaEncryptor((RSAPublicKey) publicKey,
          ((RsaCipherSpec) asymCipherSpec).getParameterSpec());
    } else {
      throw new IllegalArgumentException("Unexpected asymCipherSpec value !");
    }

    byte[] encryptedData = encryptor.encrypt(data, csprngSpec);

    logger.trace(Trace.EXIT_METHOD_1, "encryptedData", System.identityHashCode(encryptedData));
    return encryptedData;
  }

  @Override
  public byte[] asymDecrypt(CsprngSpec csprngSpec, AsymCipherSpec asymCipherSpec,
      PrivateKey privateKey, byte[] encryptedData) {
    logger.trace(Trace.ENTER_METHOD_4, "csprngSpec", csprngSpec, "asymCipherSpec", asymCipherSpec,
        "privateKey", System.identityHashCode(privateKey), "encryptedData",
        System.identityHashCode(encryptedData));

    RsaDecryptor decryptor;

    if (asymCipherSpec.getAlgo().equals(CipherAlgorithm.RSA.getAlgorithm())) {
      decryptor = new RsaDecryptor((RSAPrivateKey) privateKey,
          ((RsaCipherSpec) asymCipherSpec).getParameterSpec());
    } else {
      throw new IllegalArgumentException("Unexpected asymCipherSpec value !");
    }

    byte[] decryptedData = decryptor.decrypt(encryptedData, csprngSpec);

    logger.trace(Trace.EXIT_METHOD_1, "decryptedData", System.identityHashCode(decryptedData));
    return decryptedData;
  }

  @Override
  public SecretKey generateSharedSecretKey(CsprngSpec csprngSpec,
      KeyAgreementFnSpec keyAgreementFnSpec, KeyPair originatorKeyPair,
      PublicKey recipientPublicKey) {
    logger.trace(Trace.ENTER_METHOD_4, "csprngSpec", csprngSpec, "keyAgreementFnSpec",
        keyAgreementFnSpec, "originatorKeyPair", System.identityHashCode(originatorKeyPair),
        "recipientPublicKey", System.identityHashCode(recipientPublicKey));

    byte[] derivedKey = new byte[0];
    byte[] sharedKey = new byte[0];

    try {
      KeyAgreementProvider keyAgreementProvider = new X25519Provider();
      keyAgreementProvider.initializeAgreement(originatorKeyPair.getPrivate(), csprngSpec);
      sharedKey = keyAgreementProvider.completeAgreement(recipientPublicKey);

      // Dérive une clé écuritaire
      if (keyAgreementFnSpec.getAlgo().equals(KeyAgreementAlgorithm.X25519.getAlgorithm())
          && keyAgreementFnSpec.getParameters().get("keyBitLength").equals("256")) {
        derivedKey =
            keyAgreementProvider.deriveSecure256BitsKey(sharedKey, originatorKeyPair.getPublic(),
                recipientPublicKey);
      } else if (keyAgreementFnSpec.getAlgo().equals(KeyAgreementAlgorithm.X25519.getAlgorithm())
          && keyAgreementFnSpec.getParameters().get("keyBitLength").equals("128")) {
        derivedKey = keyAgreementProvider.deriveSecure128BitsKey(sharedKey,
            originatorKeyPair.getPublic(), recipientPublicKey);
      } else {
        throw new IllegalArgumentException("Unexpected keyAgreementFnSpec value !");
      }

      Arrays.fill(sharedKey, (byte) 0);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      Arrays.fill(sharedKey, (byte) 0);
      Arrays.fill(derivedKey, (byte) 0);

      throw new RuntimeException(e);
    }

    SecretKey secretKey =
        new SecretKeySpec(derivedKey, SecretKeyGeneratorAlgorithm.AES.getAlgorithm());
    Arrays.fill(derivedKey, (byte) 0);

    logger.trace(Trace.EXIT_METHOD_1, "secretKey", System.identityHashCode(secretKey));
    return secretKey;
  }

  @Override
  public DigitalSignature generateSignature(CsprngSpec csprngSpec, SignatureFnSpec signatureFnSpec,
      PrivateKey privateKey, byte[] data) {
    logger.trace(Trace.ENTER_METHOD_4, "csprngSpec", csprngSpec, "signatureFnSpec", signatureFnSpec,
        "privateKey", System.identityHashCode(privateKey), "data", System.identityHashCode(data));

    Signing signing;
    if (signatureFnSpec.getAlgo().equals(SignatureAlgorithm.ed25519.getAlgorithm())) {
      signing = new EdDsaSigning(privateKey);
    } else if (signatureFnSpec.getAlgo().equals(SignatureAlgorithm.RSA.getAlgorithm())) {
      signing =
          new RsaSigning(privateKey, ((RsaSignatureFnSpec) signatureFnSpec).getParameterSpec());
    } else {
      throw new IllegalArgumentException("Unexpected signatureFnSpec value !");
    }

    DigitalSignature signature = signing.sign(data, csprngSpec);

    logger.trace(Trace.EXIT_METHOD_1, "signature", System.identityHashCode(signature));
    return signature;
  }

  @Override
  public Boolean verifySignature(SignatureFnSpec signatureFnSpec, PublicKey publicKey,
      byte[] signedData, DigitalSignature signature) {
    logger.trace(Trace.ENTER_METHOD_4, "signatureFnSpec", signatureFnSpec, "publicKey",
        System.identityHashCode(publicKey), "signedData", System.identityHashCode(signedData),
        "signature", System.identityHashCode(signature));
    VerifySigning verifySigning;

    if (signatureFnSpec.getAlgo().equals(SignatureAlgorithm.ed25519.getAlgorithm())) {
      verifySigning = new EdDsaVerifySigning(publicKey);
    } else if (signatureFnSpec.getAlgo().equals(SignatureAlgorithm.RSA.getAlgorithm())) {
      verifySigning = new RsaVerifySigning(publicKey,
          ((RsaSignatureFnSpec) signatureFnSpec).getParameterSpec());
    } else {
      throw new IllegalArgumentException("Unexpected signatureFnSpec value !");
    }

    Boolean result = verifySigning.verify(signedData, signature);

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  @Override
  public Boolean verifyCertificateTemporalValidity(X509Certificate certificate) {
    logger.trace(Trace.ENTER_METHOD_1, "certificate", certificate);

    Boolean isValid =
        new TemporalVerification().verifyCertificateTemporalValidity(certificate);

    logger.trace(Trace.EXIT_METHOD_1, "isValid", isValid);
    return isValid;
  }

  @Override
  public Boolean verifyCertificateSignature(X509Certificate certificate, PublicKey publicKey) {
    logger.trace(Trace.ENTER_METHOD_2, "certificate", certificate, "publicKey",
        System.identityHashCode(publicKey));

    Boolean isValid =
        new SignatureVerification().verifyCertificateSignature(certificate, publicKey);

    logger.trace(Trace.EXIT_METHOD_1, "isValid", isValid);
    return isValid;
  }

  @Override
  public Boolean verifyCertificateChain(CertificateVerifSpec certificateVerifSpec,
      RootCertificates rootCertificates, IntermediateCertificates intermidiateCertificates,
      X509Certificate certificate) {
    logger.trace(Trace.ENTER_METHOD_4, "certificateVerifSpec", certificateVerifSpec,
        "rootCertificates", rootCertificates, "intermidiateCertificates", intermidiateCertificates,
        "certificate", certificate);

    Boolean isValid = new ChainCertificationVerification().verifyCertificateChain(
        certificateVerifSpec, rootCertificates, intermidiateCertificates, certificate, crl);

    logger.trace(Trace.EXIT_METHOD_1, "isValid", isValid);
    return isValid;
  }

  @Override
  public Boolean verifyCertificateExtensions(X509Certificate certificate, Hostname hostname,
      CertificateKeyUsages usages) {
    logger.trace(Trace.ENTER_METHOD_3, "certificate", certificate, "hostname", hostname, "usages",
        usages);

    Boolean isValid =
        new CertificateExtensionsVerification().verifyCertificateExtensions(certificate,
            hostname, usages);

    logger.trace(Trace.EXIT_METHOD_1, "isValid", isValid);
    return isValid;
  }
}
