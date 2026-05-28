package ca.griis.security.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import ca.griis.security.api.domain.spec.certificate.usage.KeyUsageType;
import ca.griis.security.api.domain.spec.cipher.asym.AsymCipherSpec;
import ca.griis.security.api.domain.spec.cipher.symm.AesCipherSpec;
import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.api.domain.spec.dh.X25519KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.generator.asym.AsymKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.Ed25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.RsaKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.asym.X25519KeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.SymKeyGenSpec;
import ca.griis.security.api.domain.spec.hash.HashFnSpec;
import ca.griis.security.api.domain.spec.sign.SignatureFnSpec;
import ca.griis.security.api.exception.DecryptException;
import ca.griis.security.api.service.DefaultSecurityService;
import ca.griis.security.spec.env.CryptoSpecId;
import ca.griis.security.spec.env.EfficientProfileSpecs;
import ca.griis.security.spec.env.StrongProfileSpecs;
import ca.griis.security.spec.env.TestInput;
import ca.griis.security.unit.util.X509CertificateCreator;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.NamedParameterSpec;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;

public final class Cases {
  private final SecurityService securityService;

  public Cases(SecurityService securityService) {
    this.securityService = securityService;
  }

  public void ct_01() {
    final Map<String, SecuritySpec> specs =
        securityService.getProfilSecuritySpecs(SecurityProfile.Strongest);

    assertThat(specs)
        .isEqualTo(StrongProfileSpecs.createSpecs());
  }

  public void ct_02() {
    final Map<String, SecuritySpec> specs =
        securityService.getProfilSecuritySpecs(SecurityProfile.Efficient);

    assertThat(specs)
        .isEqualTo(EfficientProfileSpecs.createSpecs());
  }

  public Digest ct_03(SecurityProfile profile) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var spec = (HashFnSpec) specs.get(CryptoSpecId.hashing.value());
    var data = TestInput.createData();
    var digest = securityService.generateDigest(spec, data);

    assertNotNull(digest.bytes());

    if (spec.getAlgo().equals("SHA-256")) {
      assertEquals(digest.bytes().length, 32);
    } else if (spec.getAlgo().equals("SHA-512")) {
      assertEquals(digest.bytes().length, 64);
    } else {
      assertTrue(false);
    }

    return digest;
  }

  public void ct_04(SecurityProfile profile, Digest digest) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var spec = (HashFnSpec) specs.get(CryptoSpecId.hashing.value());
    var data = TestInput.createData();
    var result = securityService.verifyDigest(spec, digest, data);

    assertTrue(result);
  }

  public void ct_05(SecurityProfile profile) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var spec = (HashFnSpec) specs.get(CryptoSpecId.hashing.value());
    var data = TestInput.createData();
    var digest = securityService.generateDigest(spec, "test".getBytes(StandardCharsets.UTF_8));
    var result = securityService.verifyDigest(spec, digest, data);

    assertFalse(result);
  }

  public SecretKey ct_06(SecurityProfile profile) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);
    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var spec = (SymKeyGenSpec) specs.get(CryptoSpecId.aesGen.value());
    var secretKey = securityService.generateSecretKey(csprngSpec, spec);

    assertEquals(secretKey.getEncoded().length, spec.getKeyBitLength() / 8);
    return secretKey;
  }

  public byte[] ct_07(SecurityProfile profile, SecretKey secretKey) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var spec = (AesCipherSpec) specs.get(CryptoSpecId.aesGcm.value());
    var data = TestInput.createData();
    var encryptedData = securityService.symEncrypt(csprngSpec, spec, secretKey, data);
    securityService.symDecrypt(csprngSpec, spec, secretKey, encryptedData);
    return encryptedData;
  }

  public void ct_08(SecurityProfile profile, SecretKey secretKey, byte[] encryptedData) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var spec = (AesCipherSpec) specs.get(CryptoSpecId.aesGcm.value());
    var data = TestInput.createData();
    var decryptedData = securityService.symDecrypt(csprngSpec, spec, secretKey, encryptedData);
    assertArrayEquals(data, decryptedData);
  }

  public void ct_09(SecurityProfile profile, byte[] encryptedData) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var keyGenSpec = (SymKeyGenSpec) specs.get(CryptoSpecId.aesGen.value());
    var spec = (AesCipherSpec) specs.get(CryptoSpecId.aesGcm.value());
    var notMatchingSecretKey = securityService.generateSecretKey(csprngSpec, keyGenSpec);

    assertThrows(
        DecryptException.class, () -> {
          securityService.symDecrypt(csprngSpec, spec, notMatchingSecretKey, encryptedData);
        });
  }

  public void ct_10(SecurityProfile profile, SecretKey secretKey) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var spec = (AesCipherSpec) specs.get(CryptoSpecId.aesGcm.value());
    var data = TestInput.createData();
    var notMatchingEncryptedData = "test".getBytes(StandardCharsets.UTF_8);
    securityService.symEncrypt(csprngSpec, spec, secretKey, data);

    assertThrows(
        DecryptException.class, () -> {
          securityService.symDecrypt(csprngSpec, spec, secretKey, notMatchingEncryptedData);
        });
  }

  public KeyPair ct_11(SecurityProfile profile, String specId) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var spec = (AsymKeyPairGenSpec) specs.get(specId);
    var keyPair = securityService.generateKeyPair(csprngSpec, spec);

    if (spec.getAlgo().equals("RSA")) {
      var rsaSpec = (RsaKeyPairGenSpec) spec;
      var params = rsaSpec.getKeyGenParameterSpec();

      var puKey = (RSAPublicKey) keyPair.getPublic();
      assertEquals(puKey.getModulus().bitLength(), params.getKeysize());

      var key = (RSAPrivateKey) keyPair.getPrivate();
      assertEquals(key.getModulus().bitLength(), params.getKeysize());
    } else if (spec.getAlgo().equals("Ed25519")) {
      var keySpec = (Ed25519KeyPairGenSpec) spec;
      assertEquals(keySpec.getNamedParameterSpec(), NamedParameterSpec.ED25519);
    } else if (spec.getAlgo().equals("X25519")) {
      var keySpec = (X25519KeyPairGenSpec) spec;
      assertEquals(keySpec.getNamedParameterSpec(), NamedParameterSpec.X25519);
    } else {
      assertTrue(false);
    }

    return keyPair;
  }

  public byte[] ct_12(SecurityProfile profile, KeyPair keyPair) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var spec = (AsymCipherSpec) specs.get(CryptoSpecId.rsaCipher.value());
    var data = TestInput.createData();
    var encryptedData = securityService.asymEncrypt(csprngSpec, spec, keyPair.getPublic(), data);
    securityService.asymDecrypt(csprngSpec, spec, keyPair.getPrivate(), encryptedData);

    return encryptedData;
  }

  public void ct_13(SecurityProfile profile, KeyPair keyPair, byte[] encryptedData) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var spec = (AsymCipherSpec) specs.get(CryptoSpecId.rsaCipher.value());
    var data = TestInput.createData();
    var decryptedData =
        securityService.asymDecrypt(csprngSpec, spec, keyPair.getPrivate(), encryptedData);
    assertArrayEquals(data, decryptedData);
  }

  public void ct_14(SecurityProfile profile, byte[] encryptedData) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var keyGenSpec = (AsymKeyPairGenSpec) specs.get(CryptoSpecId.rsaGen.value());
    var spec = (AsymCipherSpec) specs.get(CryptoSpecId.rsaCipher.value());
    var notMatchingSecretKey = securityService.generateKeyPair(csprngSpec, keyGenSpec);

    assertThrows(
        DecryptException.class, () -> {
          securityService.asymDecrypt(csprngSpec, spec, notMatchingSecretKey.getPrivate(),
              encryptedData);
        });
  }

  public void ct_15(SecurityProfile profile, KeyPair keyPair) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var spec = (AsymCipherSpec) specs.get(CryptoSpecId.rsaCipher.value());
    var data = TestInput.createData();
    var notMatchingEncryptedData = "test".getBytes(StandardCharsets.UTF_8);
    securityService.asymEncrypt(csprngSpec, spec, keyPair.getPublic(), data);

    assertThrows(
        DecryptException.class, () -> {
          securityService.asymDecrypt(csprngSpec, spec, keyPair.getPrivate(),
              notMatchingEncryptedData);
        });
  }

  public void ct_16(SecurityProfile profile) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var genSpec = (AsymKeyPairGenSpec) specs.get(CryptoSpecId.x25519Gen.value());
    var spec = (X25519KeyAgreementFnSpec) specs.get(CryptoSpecId.x25519.value());
    var originatorKeyPair = securityService.generateKeyPair(csprngSpec, genSpec);
    var recipientKeyPair = securityService.generateKeyPair(csprngSpec, genSpec);

    var secretKey = securityService.generateSharedSecretKey(csprngSpec, spec, originatorKeyPair,
        recipientKeyPair.getPublic());

    assertEquals(secretKey.getEncoded().length, spec.getKeyBitLength() / 8);
  }

  public Map.Entry<PublicKey, DigitalSignature> ct_17(SecurityProfile profile,
      String signatureSpecId,
      String specGenId) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var spec = (SignatureFnSpec) specs.get(signatureSpecId);
    var genSpec = (AsymKeyPairGenSpec) specs.get(specGenId);
    var keyPair = securityService.generateKeyPair(csprngSpec, genSpec);
    var key = keyPair.getPrivate();
    var data = TestInput.createData();

    DigitalSignature signature =
        securityService.generateSignature(csprngSpec, spec, key, data);

    if (signatureSpecId.equals(CryptoSpecId.rsaSignature.value())) {
      var privateKey = (RSAPrivateKey) key;
      assertEquals(signature.bytes().length, privateKey.getModulus().bitLength() / 8);
    } else if (signatureSpecId.equals(CryptoSpecId.ed25519Signature.value())) {
      assertEquals(signature.bytes().length, 512 / 8);
    } else {
      assertFalse(true);
    }

    return Map.entry(keyPair.getPublic(), signature);
  }

  public void ct_18(SecurityProfile profile, String signatureSpecId, PublicKey key,
      DigitalSignature signature) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);
    var spec = (SignatureFnSpec) specs.get(signatureSpecId);
    var data = TestInput.createData();
    Boolean result = securityService.verifySignature(spec, key, data, signature);
    assertTrue(result);
  }

  public void ct_19(SecurityProfile profile, String signatureSpecId, PublicKey key) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);
    var spec = (SignatureFnSpec) specs.get(signatureSpecId);
    var data = TestInput.createData();
    var notMatchingDigitalSignature = new DigitalSignature("test".getBytes(StandardCharsets.UTF_8));
    Boolean result = securityService.verifySignature(spec, key, data, notMatchingDigitalSignature);
    assertFalse(result);
  }

  public void ct_20(SecurityProfile profile, String signatureSpecId, PublicKey key,
      DigitalSignature signature) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);
    var spec = (SignatureFnSpec) specs.get(signatureSpecId);
    var notMatchingData = TestInput.createData();
    Boolean result = securityService.verifySignature(spec, key, notMatchingData, signature);
    assertTrue(result);
  }

  public void ct_21(SecurityProfile profile, String signatureSpecId, String genSpecId,
      DigitalSignature signature) {
    final Map<String, SecuritySpec> specs = securityService.getProfilSecuritySpecs(profile);
    var spec = (SignatureFnSpec) specs.get(signatureSpecId);
    var data = TestInput.createData();

    var csprngSpec = (CsprngSpec) specs.get(CryptoSpecId.csprng.value());
    var genSpec = (AsymKeyPairGenSpec) specs.get(genSpecId);
    var keyPair = securityService.generateKeyPair(csprngSpec, genSpec);
    var notMatchingPublicKey = keyPair.getPublic();

    Boolean result = securityService.verifySignature(spec, notMatchingPublicKey, data, signature);
    assertFalse(result);
  }

  public void ct_22() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificateWithSAN(
        "CN=test.ca",
        "CN=test.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "test.ca");

    Boolean result = securityService.verifyCertificateTemporalValidity(cert);
    assertTrue(result);
  }

  public void ct_23() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificateWithSAN(
        "CN=test.ca",
        "CN=test.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        true,
        "test.ca");

    Boolean result = securityService.verifyCertificateTemporalValidity(cert);
    assertFalse(result);
  }

  public void ct_24() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificateWithSAN(
        "CN=test.ca",
        "CN=test.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "test.ca");

    Boolean result = securityService.verifyCertificateSignature(cert, key.getPublic());
    assertTrue(result);
  }

  public void ct_25() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificateWithSAN(
        "CN=test.ca",
        "CN=test.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "test.ca");

    KeyPair scdKeyPair = X509CertificateCreator.generateKeyPair();
    Boolean result = securityService.verifyCertificateSignature(cert, scdKeyPair.getPublic());
    assertFalse(result);
  }

  public void ct_26() throws Exception {
    final KeyPair rootKey = X509CertificateCreator.generateKeyPair();
    final KeyPair interKey = X509CertificateCreator.generateKeyPair();
    final KeyPair leafKey = X509CertificateCreator.generateKeyPair();

    X509Certificate rootCert = X509CertificateCreator.createCertificateWithSAN(
        "CN=RootCA",
        "CN=RootCA",
        rootKey.getPublic(),
        rootKey.getPublic(), // pour AKI
        rootKey.getPrivate(),
        true,
        false,
        "test.ca");

    X509Certificate interCert = X509CertificateCreator.createCertificateWithSAN(
        "CN=IntermediateCA",
        "CN=RootCA",
        interKey.getPublic(),
        rootKey.getPublic(), // signé par root
        rootKey.getPrivate(),
        true,
        false,
        "test.ca");

    X509Certificate leafCert = X509CertificateCreator.createCertificateWithSAN(
        "CN=Leaf",
        "CN=IntermediateCA",
        leafKey.getPublic(),
        interKey.getPublic(), // signé par intermédiaire
        interKey.getPrivate(),
        false,
        false,
        "test.ca");

    RootCertificates certificates = new RootCertificates(List.of(rootCert));
    IntermediateCertificates intermediates = new IntermediateCertificates(List.of(interCert));

    final X509CRL rootCrl = X509CertificateCreator.createCRL(
        "CN=RootCA",
        rootKey.getPrivate(),
        rootCert,
        null);

    final X509CRL intermediateCRL = X509CertificateCreator.createCRL(
        "CN=IntermediateCA",
        interKey.getPrivate(),
        interCert,
        null);

    DefaultSecurityService service = new DefaultSecurityService(List.of(rootCrl, intermediateCRL));
    Boolean result = service.verifyCertificateChain(new CertificateVerifSpec(),
        certificates, intermediates, leafCert);
    assertTrue(result);
  }

  public void ct_27() throws Exception {
    final KeyPair rootKey = X509CertificateCreator.generateKeyPair();
    final KeyPair scdRootKey = X509CertificateCreator.generateKeyPair();
    final KeyPair interKey = X509CertificateCreator.generateKeyPair();
    final KeyPair leafKey = X509CertificateCreator.generateKeyPair();

    X509Certificate rootCert = X509CertificateCreator.createCertificateWithSAN(
        "CN=RootCA",
        "CN=RootCA",
        scdRootKey.getPublic(),
        scdRootKey.getPublic(), // pour AKI
        scdRootKey.getPrivate(),
        true,
        false,
        "test.ca");

    X509Certificate interCert = X509CertificateCreator.createCertificateWithSAN(
        "CN=IntermediateCA",
        "CN=RootCA",
        interKey.getPublic(),
        rootKey.getPublic(), // signé par root
        rootKey.getPrivate(),
        true,
        false,
        "test.ca");

    X509Certificate leafCert = X509CertificateCreator.createCertificateWithSAN(
        "CN=Leaf",
        "CN=IntermediateCA",
        leafKey.getPublic(),
        interKey.getPublic(), // signé par intermédiaire
        interKey.getPrivate(),
        false,
        false,
        "test.ca");

    RootCertificates certificates = new RootCertificates(List.of(rootCert));
    IntermediateCertificates intermediates = new IntermediateCertificates(List.of(interCert));

    Boolean result = securityService.verifyCertificateChain(new CertificateVerifSpec(),
        certificates, intermediates, leafCert);
    assertFalse(result);
  }

  public void ct_28() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificateWithSAN(
        "CN=example.ca",
        "CN=example.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "test.ca");

    final Hostname hostname = new Hostname("test.ca");
    final CertificateKeyUsages keyUsages = new CertificateKeyUsages(
        Set.of(KeyUsageType.DIGITAL_SIGNATURE, KeyUsageType.KEY_ENCIPHERMENT));

    Boolean result = securityService.verifyCertificateExtensions(cert, hostname, keyUsages);
    assertTrue(result);
  }

  public void ct_29() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificateWithSAN(
        "CN=test.ca",
        "CN=test.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "example.ca");

    final Hostname hostname = new Hostname("test.ca");
    final CertificateKeyUsages keyUsages = new CertificateKeyUsages(
        Set.of(KeyUsageType.DIGITAL_SIGNATURE, KeyUsageType.KEY_ENCIPHERMENT));

    Boolean result = securityService.verifyCertificateExtensions(cert, hostname, keyUsages);
    assertTrue(result);
  }

  public void ct_30() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificateWithSAN(
        "CN=test.ca",
        "CN=test.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        true,
        false,
        "test.ca");

    final Hostname hostname = new Hostname("https://test.ca");
    final CertificateKeyUsages keyUsages = new CertificateKeyUsages(
        Set.of(KeyUsageType.DIGITAL_SIGNATURE, KeyUsageType.KEY_ENCIPHERMENT));

    // Certificat racine ont une contrainte différente de -1.

    Boolean result = securityService.verifyCertificateExtensions(cert, hostname, keyUsages);
    assertFalse(result);
  }

  public void ct_31() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificateWithSAN(
        "CN=test.ca",
        "CN=test.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "test.ca");

    // L'ensemble d'usages est vide.

    final Hostname hostname = new Hostname("test.ca");
    final CertificateKeyUsages keyUsages = new CertificateKeyUsages(Set.of());

    Boolean result = securityService.verifyCertificateExtensions(cert, hostname, keyUsages);
    assertFalse(result);
  }

  public void ct_32() throws Exception {
    final KeyPair key = X509CertificateCreator.generateKeyPair();
    final X509Certificate cert = X509CertificateCreator.createCertificateWithSAN(
        "CN=example.ca",
        "CN=example.ca",
        key.getPublic(),
        key.getPublic(),
        key.getPrivate(),
        false,
        false,
        "example.com");

    // Le hostname est différente du SAN.

    final Hostname hostname = new Hostname("test.ca");
    final CertificateKeyUsages keyUsages = new CertificateKeyUsages(
        Set.of(KeyUsageType.DIGITAL_SIGNATURE, KeyUsageType.KEY_ENCIPHERMENT));

    Boolean result = securityService.verifyCertificateExtensions(cert, hostname, keyUsages);
    assertFalse(result);
  }
}
