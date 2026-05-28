/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe RsaKeysGenerator.
 *
 * @brief @~english Declaration of the RsaKeysGenerator class.
 */

package ca.griis.security.internal.asymmetric.generator;

import ca.griis.security.internal.algorithm.KeyPairGeneratorAlgorithm;
import java.security.spec.RSAKeyGenParameterSpec;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details «Detailed description of the component (optional)»
 * @par Model «Model (Abstract, automation, etc.) (optional)»
 * @par Conception «Conception description (criteria and constraints) (optional)»
 * @par Limits «Limits description (optional)»
 *
 * @brief @~french Générateur d'une paire de clés asymétriques pour l'algorithme de chiffrement RSA.
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
public class RsaKeysGenerator extends AsymKeysGenerator {
  public RsaKeysGenerator(Integer keySize) {
    super(KeyPairGeneratorAlgorithm.RSA,
        new RSAKeyGenParameterSpec(keySize, RSAKeyGenParameterSpec.F4));
  }
}
