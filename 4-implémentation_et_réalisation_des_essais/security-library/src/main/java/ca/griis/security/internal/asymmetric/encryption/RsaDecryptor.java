/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe RsaDecryptor.
 * @brief @~english Declaration of the RsaDecryptor class.
 */

package ca.griis.security.internal.asymmetric.encryption;

import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.security.internal.algorithm.CipherAlgorithm;
import ca.griis.security.internal.encryption.Decryptor;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.AlgorithmParameterSpec;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details «Detailed description of the component (optional)»
 * @par Model «Model (Abstract, automation, etc.) (optional)»
 * @par Conception «Conception description (criteria and constraints) (optional)»
 * @par Limits «Limits description (optional)»
 *
 * @brief @~french Déchiffreur asymétrique qui utilise l'algorithme de chiffrement RSA.
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
 * @par Tâches S.O.
 *      S.O.
 */
public class RsaDecryptor extends Decryptor {
  private AlgorithmParameterSpec parameterSpec;

  public RsaDecryptor(RSAPrivateKey key, AlgorithmParameterSpec parameterSpec) {
    super(CipherAlgorithm.RSA, key);
    this.parameterSpec = parameterSpec;
  }

  @Override
  public byte[] decrypt(byte[] encryptedMessage, CsprngSpec csprngSpec) {
    return decrypt(encryptedMessage, parameterSpec, csprngSpec);
  }
}
