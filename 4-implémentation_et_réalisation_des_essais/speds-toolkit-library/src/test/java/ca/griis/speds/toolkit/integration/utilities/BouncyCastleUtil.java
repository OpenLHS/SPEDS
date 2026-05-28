package ca.griis.speds.toolkit.integration.utilities;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Arrays;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class BouncyCastleUtil {
  public static byte[] encryptWithBouncyCastle(SecretKey key, byte[] data) throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");

    byte[] iv = new byte[12];
    new SecureRandom().nextBytes(iv);

    cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
    byte[] encrypted = cipher.doFinal(data);

    byte[] result = new byte[iv.length + encrypted.length];
    System.arraycopy(iv, 0, result, 0, iv.length);
    System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

    return result;
  }

  public static SecretKey generateSymmetricKeyWithBouncyCastle() throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
    keyGen.init(256);
    SecretKey secretKey = keyGen.generateKey();
    return secretKey;
  }

  public static byte[] decryptWithBouncyCastle(SecretKey key, byte[] encryptedData)
      throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    byte[] iv = new byte[12];
    byte[] ciphertext = new byte[encryptedData.length - 12];

    System.arraycopy(encryptedData, 0, iv, 0, 12);
    System.arraycopy(encryptedData, 12, ciphertext, 0, ciphertext.length);

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
    GCMParameterSpec spec = new GCMParameterSpec(128, iv);
    cipher.init(Cipher.DECRYPT_MODE, key, spec);

    return cipher.doFinal(ciphertext);
  }

  public static byte[] encryptAsymmetricSha256WithBouncyCastle(PublicKey publicKey, byte[] data)
      throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
        "SHA-256",
        "MGF1",
        MGF1ParameterSpec.SHA256,
        PSource.PSpecified.DEFAULT);

    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding", "BC");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepSpec);

    return cipher.doFinal(data);
  }

  public static byte[] encryptAsymmetricSha512WithBouncyCastle(PublicKey publicKey, byte[] data)
      throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
        "SHA-512",
        "MGF1",
        MGF1ParameterSpec.SHA512,
        PSource.PSpecified.DEFAULT);

    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding", "BC");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepSpec);

    return cipher.doFinal(data);
  }

  public static byte[] decryptAsymmetricWithBouncyCastle(PrivateKey privateKey,
      byte[] encryptedData) throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    List<OAEPParameterSpec> specs = Arrays.asList(
        new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT),
        new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1,
            PSource.PSpecified.DEFAULT),
        new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT),
        new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1,
            PSource.PSpecified.DEFAULT),
        new OAEPParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512,
            PSource.PSpecified.DEFAULT),
        new OAEPParameterSpec("SHA-384", "MGF1", MGF1ParameterSpec.SHA384,
            PSource.PSpecified.DEFAULT));

    Exception lastException = null;

    for (OAEPParameterSpec spec : specs) {
      try {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey, spec);
        return cipher.doFinal(encryptedData);
      } catch (Exception e) {
        lastException = e;
      }
    }

    String[] transformations = {
        "RSA/ECB/OAEPWithSHA-256AndMGF1Padding",
        "RSA/ECB/OAEPWithSHA-1AndMGF1Padding",
        "RSA/ECB/OAEPWithSHA-512AndMGF1Padding",
        "RSA/ECB/OAEPWithSHA-384AndMGF1Padding"
    };

    for (String transformation : transformations) {
      try {
        Cipher cipher = Cipher.getInstance(transformation, "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
      } catch (Exception e) {
        lastException = e;
      }
    }

    throw new Exception("Unable to decrypt: no matching OAEP parameters found", lastException);
  }

  public static byte[] hashSha256WithBouncyCastle(byte[] data) throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
    return digest.digest(data);
  }

  public static byte[] hashSha512WithBouncyCastle(byte[] data) throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    MessageDigest digest = MessageDigest.getInstance("SHA-512", "BC");
    return digest.digest(data);
  }

  public static boolean validateSignSha256WithBouncyCastle(byte[] digitalSignature,
      PublicKey publicKey, byte[] data) throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    Signature signature = Signature.getInstance("SHA256withRSAandMGF1", "BC");
    signature.initVerify(publicKey);
    signature.update(data);

    return signature.verify(digitalSignature);
  }

  public static boolean validateSignSha512WithBouncyCastle(byte[] digitalSignature,
      PublicKey publicKey, byte[] data) throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    Signature signature = Signature.getInstance("SHA512withRSAandMGF1", "BC");
    signature.initVerify(publicKey);
    signature.update(data);

    return signature.verify(digitalSignature);
  }

  public static byte[] signSha256WithBouncyCastle(PrivateKey privateKey, byte[] data)
      throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    PSSParameterSpec pssSpec = new PSSParameterSpec(
        "SHA-256",
        "MGF1",
        MGF1ParameterSpec.SHA256,
        32,
        1);

    Signature signature = Signature.getInstance("RSASSA-PSS", "BC");
    signature.setParameter(pssSpec);
    signature.initSign(privateKey);
    signature.update(data);

    return signature.sign();
  }

  public static byte[] signSha512WithBouncyCastle(PrivateKey privateKey, byte[] data)
      throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    PSSParameterSpec pssSpec = new PSSParameterSpec(
        "SHA-512",
        "MGF1",
        MGF1ParameterSpec.SHA512,
        64,
        1);

    Signature signature = Signature.getInstance("RSASSA-PSS", "BC");
    signature.setParameter(pssSpec);
    signature.initSign(privateKey);
    signature.update(data);

    return signature.sign();
  }


  public static KeyPair generateECDHKeyPairWithBouncyCastle(PublicKey referenceKey)
      throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    String algorithm = referenceKey.getAlgorithm();

    if (algorithm.equals("XDH")) {
      byte[] encoded = referenceKey.getEncoded();
      String curve = encoded.length <= 50 ? "X25519" : "X448";

      KeyPairGenerator keyGen = KeyPairGenerator.getInstance(curve, "BC");
      return keyGen.generateKeyPair();

    } else if (algorithm.equals("EC")) {
      ECPublicKey ecPublicKey = (ECPublicKey) referenceKey;
      ECParameterSpec ecSpec = ecPublicKey.getParameters();

      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", "BC");
      keyGen.initialize(ecSpec);

      return keyGen.generateKeyPair();

    } else {
      throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
    }
  }

  public static byte[] computeECDHSharedSecretWithBouncyCastle(
      PrivateKey myPrivateKey,
      PublicKey otherPublicKey) throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    String algorithm = myPrivateKey.getAlgorithm();

    String agreementAlgo = algorithm.equals("EC") ? "ECDH" : algorithm;

    KeyAgreement keyAgreement = KeyAgreement.getInstance(agreementAlgo, "BC");
    keyAgreement.init(myPrivateKey);
    keyAgreement.doPhase(otherPublicKey, true);

    return keyAgreement.generateSecret();
  }

  public static SecretKey generateSecretKeyFromDHWithBouncyCastle(
      Integer keyBitLength,
      byte[] sharedSecret,
      PublicKey selfPk,
      PublicKey secondPk) throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    if (keyBitLength % 8 != 0) {
      throw new IllegalArgumentException("Key size must be a multiple of 8");
    }

    final var keySizeBytes = keyBitLength / 8;
    final List<byte[]> keys = Arrays.asList(selfPk.getEncoded(), secondPk.getEncoded());
    keys.sort(Arrays::compare);

    final HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());

    final byte[] x = keys.get(0);
    final byte[] y = keys.get(1);
    byte[] info = new byte[x.length + y.length];
    System.arraycopy(x, 0, info, 0, x.length);
    System.arraycopy(y, 0, info, x.length, y.length);

    final HKDFParameters params = new HKDFParameters(sharedSecret, null, info);
    hkdf.init(params);

    byte[] derivedKey = new byte[keySizeBytes];
    hkdf.generateBytes(derivedKey, 0, derivedKey.length);

    return new SecretKeySpec(derivedKey, "AES");
  }
}
