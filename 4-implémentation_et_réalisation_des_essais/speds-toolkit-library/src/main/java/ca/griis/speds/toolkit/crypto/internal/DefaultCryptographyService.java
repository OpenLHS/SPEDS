/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe DefaultCryptographyService.
 * @brief @~english Contains description of DefaultCryptographyService class.
 */

package ca.griis.speds.toolkit.crypto.internal;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.security.api.SecurityService;
import ca.griis.security.api.domain.DigitalSignature;
import ca.griis.security.api.domain.spec.cipher.asym.AsymCipherSpec;
import ca.griis.security.api.domain.spec.cipher.symm.SymCipherSpec;
import ca.griis.security.api.domain.spec.dh.KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.generator.asym.AsymKeyPairGenSpec;
import ca.griis.security.api.domain.spec.generator.symm.SymKeyGenSpec;
import ca.griis.security.api.domain.spec.hash.HashFnSpec;
import ca.griis.security.api.domain.spec.sign.SignatureFnSpec;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.api.exception.NoSuchAlgorithmException;
import ca.griis.speds.toolkit.crypto.api.exception.NoSuchCategoryException;
import ca.griis.speds.toolkit.crypto.internal.provider.CsprngSpecProvider;
import ca.griis.speds.toolkit.crypto.internal.provider.SpecAlgoProvider;
import ca.griis.speds.toolkit.crypto.internal.provider.SpecProvider;
import ca.griis.speds.toolkit.crypto.internal.reader.CipherSuiteDtoReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

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
 * @brief @~french Implémente le service de primitives cryptographiques.
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
 *      2025-11-25 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class DefaultCryptographyService implements CryptographyService {
  private final SecurityService service;
  private final CipherSuiteDtoReader reader;

  public DefaultCryptographyService(SecurityService service, CipherSuiteDtoReader reader) {
    this.service = service;
    this.reader = reader;
  }

  @Override
  public byte[] encryptSymmetric(SpedsLayer spedsLayer, SecretKey secretKey, byte[] data) {
    var csprngSpec = new CsprngSpecProvider().getSpec();
    var spec = (SymCipherSpec) new SpecProvider(service, reader).getSpec(spedsLayer,
        AlgorithmCategory.SYMM, false).get();
    var result = service.symEncrypt(csprngSpec, spec, secretKey, data);
    return result;
  }

  @Override
  public byte[] decryptSymmetric(SpedsLayer spedsLayer, SecretKey secretKey, byte[] data) {
    var csprngSpec = new CsprngSpecProvider().getSpec();
    var spec = (SymCipherSpec) new SpecProvider(service, reader).getSpec(spedsLayer,
        AlgorithmCategory.SYMM, false).get();
    var result = service.symDecrypt(csprngSpec, spec, secretKey, data);
    return result;
  }

  @Override
  public SecretKey generateSymmetricKey(SpedsLayer spedsLayer) {
    var csprngSpec = new CsprngSpecProvider().getSpec();
    var spec = (SymKeyGenSpec) new SpecProvider(service, reader).getSpec(spedsLayer,
        AlgorithmCategory.SYMM, true).get();
    var result = service.generateSecretKey(csprngSpec, spec);
    return result;
  }

  @Override
  public byte[] encryptAsymmetric(SpedsLayer spedsLayer, PublicKey publicKey, byte[] data) {
    var csprngSpec = new CsprngSpecProvider().getSpec();
    var spec = (AsymCipherSpec) new SpecProvider(service, reader).getSpec(spedsLayer,
        AlgorithmCategory.ASYM, false).get();
    var result = service.asymEncrypt(csprngSpec, spec, publicKey, data);
    return result;
  }

  @Override
  public byte[] decryptAsymmetric(SpedsLayer spedsLayer, PrivateKey privateKey, byte[] data) {
    var csprngSpec = new CsprngSpecProvider().getSpec();
    var spec = (AsymCipherSpec) new SpecProvider(service, reader).getSpec(spedsLayer,
        AlgorithmCategory.ASYM, false).get();
    var result = service.asymDecrypt(csprngSpec, spec, privateKey, data);
    return result;
  }

  @Override
  public byte[] hash(SpedsLayer spedsLayer, byte[] data) {
    var spec = (HashFnSpec) new SpecProvider(service, reader).getSpec(spedsLayer,
        AlgorithmCategory.HASH, false).get();
    var digest = service.generateDigest(spec, data);
    var result = digest.bytes();
    return result;
  }

  @Override
  public byte[] sign(SpedsLayer spedsLayer, PrivateKey privateKey, byte[] data) {
    var csprngSpec = new CsprngSpecProvider().getSpec();
    var spec = (SignatureFnSpec) new SpecProvider(service, reader).getSpec(spedsLayer,
        AlgorithmCategory.SIGN, false).get();
    var signature = service.generateSignature(csprngSpec, spec, privateKey, data);
    var result = signature.bytes();
    return result;
  }

  @Override
  public Boolean checkSignatureValidity(SpedsLayer spedsLayer, byte[] digitalSignature,
      PublicKey publicKey, byte[] data) {
    var spec = (SignatureFnSpec) new SpecProvider(service, reader).getSpec(spedsLayer,
        AlgorithmCategory.SIGN, false).get();
    var signature = new DigitalSignature(digitalSignature);
    var result = service.verifySignature(spec, publicKey, data, signature);
    return result;
  }

  @Override
  public KeyPair chooseDiffieHellmanValue(SpedsLayer spedsLayer) {
    var csprngSpec = new CsprngSpecProvider().getSpec();
    var spec = (AsymKeyPairGenSpec) new SpecProvider(service, reader).getSpec(spedsLayer,
        AlgorithmCategory.DH, true).get();
    var result = service.generateKeyPair(csprngSpec, spec);
    return result;
  }

  @Override
  public SecretKey getDiffieHellmanSecretKey(SpedsLayer spedsLayer, KeyPair choiceX,
      PublicKey choiceY) {
    var csprngSpec = new CsprngSpecProvider().getSpec();
    var spec = (KeyAgreementFnSpec) new SpecProvider(service, reader).getSpec(spedsLayer,
        AlgorithmCategory.DH, false).get();
    var result = service.generateSharedSecretKey(csprngSpec, spec, choiceX, choiceY);
    return result;
  }

  @Override
  public String getAlgorithm(SpedsLayer spedsLayer, AlgorithmCategory category) {
    var provider = new SpecProvider(service, reader);
    var spec = provider.getSpec(spedsLayer, category, false);

    if (spec.isEmpty()) {
      throw new NoSuchCategoryException("spedsLayer: " + spedsLayer + ", category: " + category);
    }

    var algo = spec.get().getAlgo();
    return algo;
  }

  @Override
  public Map<String, String> getAlgorithmParameters(SpedsLayer spedsLayer, String algorithm) {
    final Map<String, String> params = new HashMap<>();
    final var provider = new SpecAlgoProvider(new SpecProvider(service, reader));
    final var spec = provider.getSpec(spedsLayer, algorithm);

    if (spec.isPresent()) {
      params.putAll(spec.get().getParameters());
    } else {
      throw new NoSuchAlgorithmException("spedsLayer: " + spedsLayer + ", algorithm: " + algorithm);
    }

    return params;
  }
}
