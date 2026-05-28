package ca.griis.security.unit.util.generator.asymm;

import ca.griis.security.internal.algorithm.KeyPairGeneratorAlgorithm;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

public class KeyPairGeneratorHelper {

  public KeyPair generateKeyPair(KeyPairGeneratorAlgorithm algorithm,
      AlgorithmParameterSpec algorithmParameter) {

    KeyPair keyPair;
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm.getAlgorithm());
      keyGen.initialize(algorithmParameter);
      keyPair = keyGen.generateKeyPair();
    } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
      throw new SecurityException("Unable to generate KeyPair", e);
    }

    return keyPair;
  }
}
