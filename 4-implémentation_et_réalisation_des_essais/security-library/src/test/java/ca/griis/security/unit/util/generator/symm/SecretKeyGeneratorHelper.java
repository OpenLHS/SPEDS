package ca.griis.security.unit.util.generator.symm;

import ca.griis.security.internal.algorithm.SecretKeyGeneratorAlgorithm;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SecretKeyGeneratorHelper {
  public SecretKey generateSymmetricKey(SecretKeyGeneratorAlgorithm algorithm,
      Integer keyBitLength) {

    SecretKey secretKey;
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance(algorithm.getAlgorithm());
      keyGen.init(keyBitLength);
      secretKey = keyGen.generateKey();
    } catch (NoSuchAlgorithmException | InvalidParameterException e) {
      throw new SecurityException("Unable to generate secretKey", e);
    }

    return secretKey;
  }
}
