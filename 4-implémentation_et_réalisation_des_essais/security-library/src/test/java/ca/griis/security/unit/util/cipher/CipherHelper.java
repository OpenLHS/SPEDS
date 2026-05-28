package ca.griis.security.unit.util.cipher;

import ca.griis.security.internal.algorithm.CipherAlgorithm;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;

public class CipherHelper {
  private static final Integer gcmTagBitSize = 128;
  private static final Integer gcmIvLength = 96;
  private static SecureRandom random = new SecureRandom();

  public byte[] encrypt(
      byte[] message, CipherAlgorithm cipherAlgorithm, Key key,
      AlgorithmParameterSpec algorithmParameterSpec) {

    byte[] encrypted;
    try {
      Cipher cipherAlgo = Cipher.getInstance(cipherAlgorithm.getAlgorithm());
      cipherAlgo.init(Cipher.ENCRYPT_MODE, key, algorithmParameterSpec);
      encrypted = cipherAlgo.doFinal(message);
    } catch (InvalidKeyException
        | IllegalBlockSizeException
        | BadPaddingException
        | NoSuchAlgorithmException
        | NoSuchPaddingException
        | InvalidAlgorithmParameterException e) {
      throw new SecurityException("Unable to encrypt a message if no public key is provided!", e);
    }

    return encrypted;
  }

  public byte[] encryptSymm(byte[] message, Key key) {
    byte[] iv = createRandomIv();
    GCMParameterSpec parameterSpec = new GCMParameterSpec(gcmTagBitSize, iv);

    byte[] encrypted = encrypt(message, CipherAlgorithm.AESGCM, key, parameterSpec);

    byte[] encryptedWithIv = assembleEncryptedWithIv(iv, encrypted);
    return encryptedWithIv;
  }

  private static byte[] assembleEncryptedWithIv(byte[] iv, byte[] encrypted) {
    byte[] encryptedWithIv =
        ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
    return encryptedWithIv;
  }

  private static byte[] createRandomIv() {
    byte[] iv = new byte[gcmIvLength / 8];
    random.nextBytes(iv);
    return iv;
  }
}
