package ca.griis.security.unit.util.hash;

import ca.griis.security.api.domain.Digest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingHelper {
  public Digest hash(byte[] bytes, String algorithm) {
    MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new SecurityException(e);
    }
    return new Digest(messageDigest.digest(bytes));
  }
}
