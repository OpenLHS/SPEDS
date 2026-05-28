/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe AsymKeysGenerator.
 * @brief @~english Declaration of the AsymKeysGenerator class.
 */

package ca.griis.security.internal.asymmetric.generator;

import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.internal.algorithm.KeyPairGeneratorAlgorithm;
import ca.griis.security.internal.random.RandomProvider;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details «Detailed description of the component (optional)»
 * @par Model «Model (Abstract, automation, etc.) (optional)»
 * @par Conception «Conception description (criteria and constraints) (optional)»
 * @par Limits «Limits description (optional)»
 *
 * @brief @~french Générateur d'une paire de clés asymétriques.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 * @par Historique
 *      2025-12-17 [BD] - Implémentation de la classe.
 * @par Tâches
 *      S.O.
 */
public abstract class AsymKeysGenerator {
  private final KeyPairGeneratorAlgorithm algorithm;
  private final AlgorithmParameterSpec algorithmParameter;

  protected AsymKeysGenerator(KeyPairGeneratorAlgorithm algorithm,
      AlgorithmParameterSpec algorithmParameter) {
    this.algorithm = algorithm;
    this.algorithmParameter = algorithmParameter;
  }

  public KeyPair generateKeyPair(CsprngSpec csprngSpec) {
    SecureRandom random = RandomProvider.getSecureRandom(csprngSpec);

    KeyPair keyPair;
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm.getAlgorithm());
      keyGen.initialize(algorithmParameter, random);
      keyPair = keyGen.generateKeyPair();
    } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
      throw new SecurityException("Unable to generate a key pair", e);
    }

    return keyPair;
  }
}
