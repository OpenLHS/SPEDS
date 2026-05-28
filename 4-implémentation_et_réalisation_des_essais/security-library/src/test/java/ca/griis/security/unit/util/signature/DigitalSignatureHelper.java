package ca.griis.security.unit.util.signature;

import ca.griis.security.api.domain.DigitalSignature;
import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.internal.algorithm.SignatureAlgorithm;
import ca.griis.security.internal.random.RandomProvider;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;

public class DigitalSignatureHelper {
  protected final SignatureAlgorithm signatureAlgorithm;
  protected final PrivateKey privateKey;
  protected final AlgorithmParameterSpec params;

  public DigitalSignatureHelper(
      SignatureAlgorithm signatureAlgorithm, PrivateKey privateKey, AlgorithmParameterSpec params) {
    this.signatureAlgorithm = signatureAlgorithm;
    this.privateKey = privateKey;
    this.params = params;
  }

  public DigitalSignature sign(byte[] message, CsprngSpec csprngSpec) {

    SecureRandom random = RandomProvider.getSecureRandom(csprngSpec);

    byte[] signedHash;
    try {
      Signature signatureAlgo = Signature.getInstance(signatureAlgorithm.getAlgorithm());
      signatureAlgo.initSign(privateKey, random);

      signatureAlgo.setParameter(params);

      signatureAlgo.update(message);
      signedHash = signatureAlgo.sign();
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException
        | InvalidAlgorithmParameterException e) {
      throw new SecurityException("Unable to sign with " + privateKey.getAlgorithm(), e);
    }

    return new DigitalSignature(signedHash);
  }
}
